(ns alumbra.js.ast.javascript
  "Transform a GraphQL AST from JS to Alumbra."
  (:require [clojure.string :as str]))

(declare selection-set->alumbra type->alumbra value->alumbra)

(defn- location->alumbra
  "Convert a location from JS to Alumbra."
  [location]
  #?(:clj {:column (-> location :startToken :column)
           :index (-> location :startToken :start)
           :row (-> location :startToken :line)}
     :cljs {:column (.. location -startToken -column)
            :index (.. location -startToken -start)
            :row (.. location -startToken -line)}))

(defn- object-field->alumbra
  "Transform a GraphQL object field from JS to Alumbra."
  [{:keys [kind loc name value]}]
  {:pre [(= kind "ObjectField")]}
  {:alumbra/field-name (:value name)
   :alumbra/metadata (location->alumbra loc)
   :alumbra/value (value->alumbra value)})

(defmulti type->alumbra
  "Transform a GraphQL type from JS to Alumbra."
  (fn [{:keys [kind]}] (keyword kind)))

(defmethod type->alumbra :ListType
  [{:keys [loc type]}]
  {:alumbra/element-type (type->alumbra type)
   :alumbra/non-null? false
   :alumbra/type-class :list-type
   :alumbra/metadata (location->alumbra loc)})

(defmethod type->alumbra :NamedType
  [{:keys [name loc]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/non-null? false
   :alumbra/type-class :named-type
   :alumbra/type-name (-> name :value)})

(defn- type-condition->alumbra
  "Transform a GraphQL type condition from JS to Alumbra."
  [{:keys [loc name]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/type-name (:value name)})

(defmethod type->alumbra :NonNullType
  [{:keys [type]}]
  (assoc (type->alumbra type) :alumbra/non-null? true))

(defmulti value->alumbra
  "Transform a GraphQL value from JS to Alumbra."
  (fn [{:keys [kind]}] (keyword kind)))

(defmethod value->alumbra :BooleanValue
  [{:keys [loc value]}]
  {:alumbra/value-type :boolean
   :alumbra/boolean value
   :alumbra/metadata (location->alumbra loc)})

(defmethod value->alumbra :EnumValue
  [{:keys [loc value]}]
  {:alumbra/value-type :enum
   :alumbra/enum value
   :alumbra/metadata (location->alumbra loc)})

(defmethod value->alumbra :FloatValue
  [{:keys [loc value]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :float
   :alumbra/float
   #?(:clj (Double/parseDouble value)
      :cljs (js/parseFloat value))})

(defmethod value->alumbra :ListValue
  [{:keys [loc values]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :list
   :alumbra/list (mapv value->alumbra values)})

(defmethod value->alumbra :IntValue
  [{:keys [loc value]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :integer
   :alumbra/integer
   #?(:clj (Integer/parseInt value)
      :cljs (js/parseInt value))})

(defmethod value->alumbra :NullValue
  [{:keys [loc value]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :null})

(defmethod value->alumbra :StringValue
  [{:keys [loc value]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :string
   :alumbra/string value})

(defmethod value->alumbra :ObjectValue
  [{:keys [loc fields]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :object
   :alumbra/object (mapv object-field->alumbra fields)})

(defmethod value->alumbra :Variable
  [{:keys [loc name]}]
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/value-type :variable
   :alumbra/variable-name (:value name)})

(declare directive->alumbra)

(defn- argument->alumbra
  "Transform a GraphQL argument from JS to Alumbra."
  [{:keys [kind loc name value]}]
  {:pre  [(= kind "Argument")]}
  {:alumbra/argument-name (:value name)
   :alumbra/argument-value (value->alumbra value)
   :alumbra/metadata (location->alumbra loc)})

(defn- argument-definition->alumbra
  "Transform a GraphQL argument definition from JS to Alumbra."
  [{:keys [kind description defaultValue directives loc name type]}]
  {:pre  [(= kind "InputValueDefinition")]}
  (cond-> {:alumbra/argument-name (:value name)
           :alumbra/argument-type (type->alumbra type)
           :alumbra/metadata (location->alumbra loc)}
    defaultValue
    (assoc :alumbra/default-value (value->alumbra defaultValue))
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- directive->alumbra
  "Transform a GraphQL directive from JS to Alumbra."
  [{:keys [arguments kind loc name]}]
  {:pre  [(= kind "Directive")]}
  (cond-> {:alumbra/directive-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq arguments)
    (assoc :alumbra/arguments (mapv argument->alumbra arguments) )))

(defn- named-type->alumbra
  "Transform a GraphQL named type from JS to Alumbra."
  [{:keys [kind loc name]}]
  {:pre  [(= kind "NamedType")]}
  {:alumbra/type-name (:value name)
   :alumbra/metadata (location->alumbra loc)})

(defn- field-definition->alumbra
  "Transform a GraphQL  field definition from JS to Alumbra."
  [{:keys [arguments kind description defaultValue directives loc name type]}]
  {:pre  [(= kind "FieldDefinition")]}
  (cond-> {:alumbra/field-name (:value name)
           :alumbra/metadata (location->alumbra loc)
           :alumbra/type (type->alumbra type)}
    (seq arguments)
    (assoc :alumbra/argument-definitions (mapv argument-definition->alumbra arguments))
    defaultValue
    (assoc :alumbra/default-value (value->alumbra defaultValue))
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- interface-definition->alumbra
  "Transform a GraphQL interface definition from JS to Alumbra."
  [{:keys [description directives fields kind loc name]}]
  {:pre  [(= kind "InterfaceTypeDefinition")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/field-definitions (mapv field-definition->alumbra fields))))

(defn- interface-extension->alumbra
  "Transform a GraphQL interface extension from JS to Alumbra."
  [{:keys [description directives fields kind loc name]}]
  {:pre  [(= kind "InterfaceTypeExtension")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/field-definitions (mapv field-definition->alumbra fields))))

(defn- input-field-definition->alumbra
  "Transform a GraphQL input field definition from JS to Alumbra."
  [{:keys [kind description defaultValue directives loc name type]}]
  {:pre  [(= kind "InputValueDefinition")]}
  (cond-> {:alumbra/field-name (:value name)
           :alumbra/metadata (location->alumbra loc)
           :alumbra/type (type->alumbra type)}
    defaultValue
    (assoc :alumbra/default-value (value->alumbra defaultValue))
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- input-type-definition->alumbra
  "Transform a GraphQL input type definition from JS to Alumbra."
  [{:keys [description directives fields kind loc name]}]
  {:pre  [(= kind "InputObjectTypeDefinition")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/input-field-definitions (mapv input-field-definition->alumbra fields))))

(defn- type-definition->alumbra
  "Transform a GraphQL type definition from JS to Alumbra."
  [{:keys [description directives fields kind loc name interfaces]}]
  {:pre  [(= kind "ObjectTypeDefinition")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/field-definitions (mapv field-definition->alumbra fields))
    (seq interfaces)
    (assoc :alumbra/interface-types (mapv named-type->alumbra interfaces))))

(defn- type-extension->alumbra
  "Transform a GraphQL type extension from JS to Alumbra."
  [{:keys [description directives fields kind name loc interfaces]}]
  {:pre  [(= kind "ObjectTypeExtension")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/field-definitions (mapv field-definition->alumbra fields))
    (seq interfaces)
    (assoc :alumbra/interface-types (mapv named-type->alumbra interfaces))))

(defn- input-extension->alumbra
  "Transform a GraphQL input extension from JS to Alumbra."
  [{:keys [description directives fields kind loc name]}]
  {:pre  [(= kind "InputObjectTypeExtension")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    (seq fields)
    (assoc :alumbra/input-field-definitions (mapv input-field-definition->alumbra fields))))

(defn- operation-type-definition->alumbra
  "Transform a GraphQL operation type definition from JS to Alumbra."
  [{:keys [kind loc operation type]}]
  {:pre  [(= kind "OperationTypeDefinition")]}
  {:alumbra/metadata (location->alumbra loc)
   :alumbra/operation-type operation
   :alumbra/schema-type (named-type->alumbra type)})

(defn- schema-definition->alumbra
  "Transform a GraphQL schema definition from JS to Alumbra."
  [{:keys [directives kind loc operationTypes]}]
  {:pre  [(= kind "SchemaDefinition")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/schema-fields (mapv operation-type-definition->alumbra operationTypes)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- schema-extension->alumbra
  "Transform a GraphQL schema extension from JS to Alumbra."
  [{:keys [directives kind loc operationTypes]}]
  {:pre  [(= kind "SchemaExtension")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/schema-fields (mapv operation-type-definition->alumbra operationTypes)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defmulti ^:private selection->alumbra
  "Transform a GraphQL selection from JS to Alumbra."
  (fn [{:keys [kind]}] (keyword kind)))

(defmethod selection->alumbra :Field
  [{:keys [alias arguments directives loc name selectionSet]}]
  (cond-> {:alumbra/field-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq arguments)
    (assoc :alumbra/arguments (mapv argument->alumbra arguments))
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    alias
    (assoc :alumbra/field-alias (:value alias))
    selectionSet
    (assoc :alumbra/selection-set (selection-set->alumbra selectionSet))))

(defmethod selection->alumbra :FragmentSpread
  [{:keys [directives loc name]}]
  (cond-> {:alumbra/fragment-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defmethod selection->alumbra :InlineFragment
  [{:keys [directives loc selectionSet typeCondition]}]
  (cond-> {:alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    selectionSet
    (assoc :alumbra/selection-set (selection-set->alumbra selectionSet))
    typeCondition
    (assoc :alumbra/type-condition (type-condition->alumbra typeCondition))))

(defn- selection-set->alumbra
  "Transform a GraphQL selection set from JS to Alumbra."
  [{:keys [kind selections]}]
  {:pre  [(= kind "SelectionSet")]}
  (mapv selection->alumbra selections))

(defn- variable-definition->alumbra
  "Transform a GraphQL selection set from JS to Alumbra."
  [{:keys [defaultValue kind loc type variable]}]
  {:pre  [(= kind "VariableDefinition")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/variable-name (-> variable :name :value)
           :alumbra/type (type->alumbra type)}
    defaultValue
    (assoc :alumbra/default-value (value->alumbra defaultValue))))

(defn- fragment-definition->alumbra
  "Transform a GraphQL fragment definition from JS to Alumbra."
  [{:keys [directives kind name loc typeCondition selectionSet]}]
  {:pre  [(= kind "FragmentDefinition")]}
  (cond-> {:alumbra/fragment-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    selectionSet
    (assoc :alumbra/selection-set (selection-set->alumbra selectionSet))
    typeCondition
    (assoc :alumbra/type-condition (type-condition->alumbra typeCondition))))

(defn- operation-definition->alumbra
  "Transform a GraphQL operation definition from JS to Alumbra."
  [{:keys [directives kind loc name operation selectionSet variableDefinitions]}]
  {:pre  [(= kind "OperationDefinition")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/operation-type operation}
    (:value name)
    (assoc :alumbra/operation-name (:value name))
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))
    selectionSet
    (assoc :alumbra/selection-set (selection-set->alumbra selectionSet))
    (seq variableDefinitions)
    (assoc :alumbra/variables (mapv variable-definition->alumbra variableDefinitions))))

(defn- directive-location->alumbra
  "Transform a GraphQL directive location from JS to Alumbra."
  [{:keys [kind value]}]
  {:pre  [(= kind "Name")]}
  (-> value str/lower-case (str/replace #"_" "-") keyword))

(defn- directive-definition->alumbra
  "Transform a GraphQL directive definition from JS to Alumbra."
  [{:keys [arguments kind loc name locations]}]
  {:pre  [(= kind "DirectiveDefinition")]}
  (cond-> {:alumbra/directive-name (:value name)
           :alumbra/metadata (location->alumbra loc)}
    (seq arguments)
    (assoc :alumbra/argument-definitions (mapv argument-definition->alumbra arguments))
    (seq locations)
    (assoc :alumbra/directive-locations (mapv directive-location->alumbra locations))))

(defn- enum-value->alumbra
  "Transform a GraphQL enum value from JS to Alumbra."
  [{:keys [kind loc name]}]
  {:pre  [(= kind "EnumValueDefinition")]}
  {:alumbra/enum (:value name)
   :alumbra/metadata (location->alumbra loc)})

(defn- enum-definition->alumbra
  "Transform a GraphQL enum definition from JS to Alumbra."
  [{:keys [directives kind loc name values]}]
  {:pre  [(= kind "EnumTypeDefinition")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/metadata (location->alumbra loc)
           :alumbra/enum-fields (mapv enum-value->alumbra values)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- enum-extension->alumbra
  "Transform a GraphQL enum extension from JS to Alumbra."
  [{:keys [directives kind loc name values]}]
  {:pre  [(= kind "EnumTypeExtension")]}
  (cond-> {:alumbra/type-name (:value name)
           :alumbra/enum-fields (mapv enum-value->alumbra values)
           :alumbra/metadata (location->alumbra loc)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- scalar-definition->alumbra
  "Transform a GraphQL scalar definition from JS to Alumbra."
  [{:keys [directives kind loc name]}]
  {:pre  [(= kind "ScalarTypeDefinition")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/type-name (:value name)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- scalar-extension->alumbra
  "Transform a GraphQL scalar extension from JS to Alumbra."
  [{:keys [directives kind loc name]}]
  {:pre  [(= kind "ScalarTypeExtension")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/type-name (:value name)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- union-definition->alumbra
  "Transform a GraphQL union definition from JS to Alumbra."
  [{:keys [directives kind loc name types]}]
  {:pre  [(= kind "UnionTypeDefinition")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/type-name (:value name)
           :alumbra/union-types (mapv named-type->alumbra types)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- union-extension->alumbra
  "Transform a GraphQL union extension from JS to Alumbra."
  [{:keys [directives kind loc name types]}]
  {:pre  [(= kind "UnionTypeExtension")]}
  (cond-> {:alumbra/metadata (location->alumbra loc)
           :alumbra/type-name (:value name)
           :alumbra/union-types (mapv named-type->alumbra types)}
    (seq directives)
    (assoc :alumbra/directives (mapv directive->alumbra directives))))

(defn- filter-by-kind [kind coll]
  (filter #(= (:kind %) kind) coll))

(defn js->alumbra
  "Transform a GraphQL document from JS to Alumbra."
  [{:keys [kind definitions loc]}]
  {:pre  [(= kind "Document")]}
  (let [directive-definitions (filter-by-kind "DirectiveDefinition" definitions)
        enum-definitions (filter-by-kind "EnumTypeDefinition" definitions)
        enum-extensions (filter-by-kind "EnumTypeExtension" definitions)
        fragments (filter-by-kind "FragmentDefinition" definitions)
        input-extensions (filter-by-kind "InputObjectTypeExtension" definitions)
        input-object-type-definitions (filter-by-kind "InputObjectTypeDefinition" definitions)
        interface-definitions (filter-by-kind "InterfaceTypeDefinition" definitions)
        interface-extensions (filter-by-kind "InterfaceTypeExtension" definitions)
        operations (filter-by-kind "OperationDefinition" definitions)
        scalar-definitions (filter-by-kind "ScalarTypeDefinition" definitions)
        scalar-extensions (filter-by-kind "ScalarTypeExtension" definitions)
        schema-definitions (filter-by-kind "SchemaDefinition" definitions)
        schema-extensions (filter-by-kind "SchemaExtension" definitions)
        type-definitions (filter-by-kind "ObjectTypeDefinition" definitions)
        type-extensions (filter-by-kind "ObjectTypeExtension" definitions)
        union-definitions (filter-by-kind "UnionTypeDefinition" definitions)
        union-extensions (filter-by-kind "UnionTypeExtension" definitions)]
    (cond-> {:alumbra/metadata (location->alumbra loc)}
      (seq directive-definitions)
      (assoc :alumbra/directive-definitions (mapv directive-definition->alumbra directive-definitions))

      (seq enum-definitions)
      (assoc :alumbra/enum-definitions (mapv enum-definition->alumbra enum-definitions))

      (seq enum-extensions)
      (assoc :alumbra/enum-extensions (mapv enum-extension->alumbra enum-extensions))

      (seq fragments)
      (assoc :alumbra/fragments (mapv fragment-definition->alumbra fragments))

      (seq input-object-type-definitions)
      (assoc :alumbra/input-type-definitions (mapv input-type-definition->alumbra input-object-type-definitions))

      (seq input-extensions)
      (assoc :alumbra/input-extensions (mapv input-extension->alumbra input-extensions))

      (seq interface-definitions)
      (assoc :alumbra/interface-definitions (mapv interface-definition->alumbra interface-definitions))

      (seq interface-extensions)
      (assoc :alumbra/interface-extensions (mapv interface-extension->alumbra interface-extensions))

      (seq operations)
      (assoc :alumbra/operations (mapv operation-definition->alumbra operations))

      (seq scalar-definitions)
      (assoc :alumbra/scalar-definitions (mapv scalar-definition->alumbra scalar-definitions))

      (seq scalar-extensions)
      (assoc :alumbra/scalar-extensions (mapv scalar-extension->alumbra scalar-extensions))

      (seq schema-definitions)
      (assoc :alumbra/schema-definitions (mapv schema-definition->alumbra schema-definitions))

      (seq schema-extensions)
      (assoc :alumbra/schema-extensions (mapv schema-extension->alumbra schema-extensions))

      (seq type-definitions)
      (assoc :alumbra/type-definitions (mapv type-definition->alumbra type-definitions))

      (seq type-extensions)
      (assoc :alumbra/type-extensions (mapv type-extension->alumbra type-extensions))

      (seq union-definitions)
      (assoc :alumbra/union-definitions (mapv union-definition->alumbra union-definitions))

      (seq union-extensions)
      (assoc :alumbra/union-extensions (mapv union-extension->alumbra union-extensions)))))
