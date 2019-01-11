(ns alumbra.js.ast.alumbra
  "Transform a GraphQL AST from Alumbra to JS."
  (:require [clojure.string :as str]))

(declare type->js selection-set->js)

(defn- meatadata->loc
  "Convert Alumbra metadata to a JS location."
  [{:keys [column index row]}]
  #?(:clj {:startToken {:column column :line row :start index}}
     :cljs #js {:startToken #js {:column column :line row :start index}}))

(defn- name->js [v metadata]
  (when v
    {:kind "Name"
     :loc (meatadata->loc metadata)
     :value v}))

(defmulti value->js
  "Transform a GraphQL value from Alumbra to JS."
  :alumbra/value-type)

(defmethod value->js :boolean
  [{:keys [alumbra/boolean alumbra/metadata]}]
  {:kind "BooleanValue"
   :loc (meatadata->loc metadata)
   :value boolean})

(defmethod value->js :enum
  [{:keys [alumbra/enum alumbra/metadata]}]
  {:kind "EnumValue"
   :loc (meatadata->loc metadata)
   :value enum})

(defmethod value->js :float
  [{:keys [alumbra/float alumbra/metadata]}]
  {:kind "FloatValue"
   :loc (meatadata->loc metadata)
   :value (str float)})

(defmethod value->js :integer
  [{:keys [alumbra/integer alumbra/metadata]}]
  {:kind "IntValue"
   :loc (meatadata->loc metadata)
   :value (str integer)})

(defmethod value->js :list
  [{:keys [alumbra/list alumbra/metadata]}]
  {:kind "ListValue"
   :loc (meatadata->loc metadata)
   :values (mapv value->js list)})

(defmethod value->js :null
  [{:keys [alumbra/metadata]}]
  {:kind "NullValue"
   :loc (meatadata->loc metadata)})

(defmethod value->js :string
  [{:keys [alumbra/metadata alumbra/string]}]
  {:block false ;; TODO: What is block?
   :kind "StringValue"
   :loc (meatadata->loc metadata)
   :value string})

(defn- object-field->js
  "Transform a GraphQL object field from Alumbra to JS."
  [{:keys [alumbra/field-name alumbra/metadata alumbra/value]}]
  {:kind "ObjectField"
   :loc (meatadata->loc metadata)
   :name (name->js field-name metadata)
   :value (value->js value)})

(defmethod value->js :object
  [{:keys [alumbra/metadata alumbra/object]}]
  {:kind "ObjectValue"
   :loc (meatadata->loc metadata)
   :fields (mapv object-field->js object)})

(defmethod value->js :variable
  [{:keys [alumbra/metadata alumbra/variable-name]}]
  {:kind "Variable"
   :loc (meatadata->loc metadata)
   :name (name->js variable-name metadata)})

(defn- argument->js
  "Transform a GraphQL argument from Alumbra to JS."
  [{:keys [alumbra/argument-name alumbra/argument-value alumbra/metadata]}]
  {:kind "Argument"
   :loc (meatadata->loc metadata)
   :name (name->js argument-name metadata)
   :value (value->js argument-value)})

(declare directive->js)

(defn- argument-definition->js
  "Transform a GraphQL argument definition from Alumbra to JS."
  [{:keys [alumbra/argument-name
           alumbra/argument-type
           alumbra/directives
           alumbra/default-value
           alumbra/metadata]}]
  {:defaultValue (some-> default-value value->js)
   :description nil
   :directives (mapv directive->js directives)
   :kind "InputValueDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js argument-name metadata)
   :type (type->js argument-type)})

(defn- directive->js
  "Transform a GraphQL directive from Alumbra to JS."
  [{:keys [alumbra/arguments alumbra/metadata alumbra/directive-name]}]
  {:arguments (mapv argument->js arguments)
   :kind "Directive"
   :loc (meatadata->loc metadata)
   :name (name->js directive-name metadata)})

(defn- named-type->js
  "Transform a GraphQL type condition from Alumbra to JS."
  [{:keys [alumbra/metadata alumbra/type-name]}]
  {:kind "NamedType"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- fragment->js
  "Transform a GraphQL fragment from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/fragment-name
           alumbra/metadata
           alumbra/type-condition
           alumbra/selection-set]}]
  {:kind "FragmentDefinition"
   :name (name->js fragment-name metadata)
   :loc (meatadata->loc metadata)
   :typeCondition (named-type->js type-condition)
   :directives (mapv directive->js directives)
   :selectionSet (selection-set->js selection-set)})

(defn- fragment-spread->js
  "Transform a GraphQL fragment spread from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/fragment-name
           alumbra/metadata]}]
  {:directives (mapv directive->js directives)
   :kind "FragmentSpread"
   :loc (meatadata->loc metadata)
   :name (name->js fragment-name metadata)})

(defn- field->js
  "Transform a GraphQL field from Alumbra to JS."
  [{:keys [alumbra/arguments
           alumbra/directives
           alumbra/field-alias
           alumbra/field-name
           alumbra/metadata
           alumbra/selection-set]}]
  {:alias (name->js field-alias metadata)
   :arguments (mapv argument->js arguments)
   :directives (mapv directive->js directives)
   :kind "Field"
   :loc (meatadata->loc metadata)
   :name (name->js field-name metadata)
   :selectionSet (selection-set->js selection-set)})

(defn- field-definition->js
  "Transform a GraphQL field definition from Alumbra to JS."
  [{:keys [alumbra/arguments
           alumbra/argument-definitions
           alumbra/directives
           alumbra/field-name
           alumbra/metadata
           alumbra/type]}]
  {:arguments
   (cond
     (seq arguments)
     (mapv argument->js arguments)
     (seq argument-definitions)
     (mapv argument-definition->js argument-definitions)
     :else [])
   :kind "FieldDefinition"
   :loc (meatadata->loc metadata)
   :description nil
   :name (name->js field-name metadata)
   :type (type->js type)
   :directives (mapv directive->js directives)})

(defn- inline-fragment->js
  "Transform a GraphQL inline fragment from Alumbra to JS."
  [{:keys [alumbra/arguments
           alumbra/directives
           alumbra/field-name
           alumbra/metadata
           alumbra/selection-set
           alumbra/type-condition]}]
  {:directives (mapv directive->js directives)
   :kind "InlineFragment"
   :loc (meatadata->loc metadata)
   :selectionSet (selection-set->js selection-set)
   :typeCondition (some-> type-condition named-type->js)})

(defn- input-field-definition->js
  "Transform a GraphQL input field definition from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/default-value
           alumbra/field-name
           alumbra/metadata
           alumbra/type]}]
  {:kind "InputValueDefinition"
   :loc (meatadata->loc metadata)
   :description nil
   :name (name->js field-name metadata)
   :type (type->js type)
   :defaultValue (some-> default-value value->js)
   :directives (mapv directive->js directives)})

(defn- input-type-definition->js
  "Transform a GraphQL input type definition from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/input-field-definitions
           alumbra/metadata
           alumbra/type-name]}]
  {:description nil
   :directives (mapv directive->js directives)
   :fields (mapv input-field-definition->js input-field-definitions)
   :kind "InputObjectTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- input-extension->js
  "Transform a GraphQL input extension from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/input-field-definitions
           alumbra/metadata
           alumbra/type-name]}]
  {:directives (mapv directive->js directives)
   :fields (mapv input-field-definition->js input-field-definitions)
   :kind "InputObjectTypeExtension"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- interface-definition->js
  "Transform a GraphQL interface definition from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/field-definitions
           alumbra/metadata
           alumbra/type-name]}]
  {:description nil
   :directives (mapv directive->js directives)
   :fields (mapv field-definition->js field-definitions)
   :kind "InterfaceTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- interface-extension->js
  "Transform a GraphQL interface extension from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/field-definitions
           alumbra/metadata
           alumbra/type-name]}]
  {:kind "InterfaceTypeExtension"
   :loc (meatadata->loc metadata)
   :directives (mapv directive->js directives)
   :fields (mapv field-definition->js field-definitions)
   :name (name->js type-name metadata)})

(defn- type-definition->js
  "Transform a GraphQL type definition from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/field-definitions
           alumbra/interface-types
           alumbra/metadata
           alumbra/type-name]}]
  {:description nil
   :directives (mapv directive->js directives)
   :fields (mapv field-definition->js field-definitions)
   :interfaces (mapv named-type->js interface-types)
   :kind "ObjectTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- type-extension->js
  "Transform a GraphQL type extension from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/field-definitions
           alumbra/interface-types
           alumbra/metadata
           alumbra/type-name]}]
  {:kind "ObjectTypeExtension"
   :loc (meatadata->loc metadata)
   :directives (mapv directive->js directives)
   :fields (mapv field-definition->js field-definitions)
   :interfaces (mapv named-type->js interface-types)
   :name (name->js type-name metadata)})

(defn- selection->js
  "Transform a GraphQL selection from Alumbra to JS."
  [{:keys [alumbra/field-name
           alumbra/fragment-name
           alumbra/selection-set
           alumbra/type-condition]
    :as selection}]
  (cond
    field-name
    (field->js selection)
    fragment-name
    (fragment-spread->js selection)
    type-condition
    (inline-fragment->js selection)
    selection-set
    (inline-fragment->js selection)))

(defn- selection-set->js
  "Transform a GraphQL selection set from Alumbra to JS."
  [selections]
  (when (seq selections)
    {:kind "SelectionSet"
     ;; TODO:
     ;; :loc (meatadata->loc metadata)
     :selections (mapv selection->js selections)}))

(defmulti type-class->js
  "Transform a GraphQL type from Alumbra to JS."
  :alumbra/type-class)

(defmethod type-class->js :named-type
  [{:keys [alumbra/metadata alumbra/type-name]}]
  {:kind "NamedType"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defmethod type-class->js :list-type
  [{:keys [alumbra/element-type alumbra/metadata]}]
  {:kind "ListType"
   :loc (meatadata->loc metadata)
   :type (type->js element-type)})

(defn- type-non-null->js [type]
  (let [{:keys [alumbra/metadata alumbra/non-null?]} type]
    (assert non-null?)
    {:kind "NonNullType"
     :loc (meatadata->loc metadata)
     :type (type-class->js type)}))

(defn- type->js [type]
  (if (:alumbra/non-null? type)
    (type-non-null->js type)
    (type-class->js type)))

(defn- variable->js
  "Transform a GraphQL variable from Alumbra to JS."
  [{:keys [alumbra/default-value
           alumbra/metadata
           alumbra/type
           alumbra/variable-name]}]
  {:defaultValue (some-> default-value value->js)
   :kind "VariableDefinition"
   :loc (meatadata->loc metadata)
   :type (type->js type)
   :variable
   {:kind "Variable"
    :name (name->js variable-name metadata)}})

(defn- operation->js
  "Transform a GraphQL operation from Alumbra to JS."
  [{:keys [alumbra/directives
           alumbra/fragments
           alumbra/metadata
           alumbra/operation-type
           alumbra/operation-name
           alumbra/selection-set
           alumbra/variables]}]
  {:directives (mapv directive->js directives)
   :kind "OperationDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js operation-name metadata)
   :operation operation-type
   :selectionSet (selection-set->js selection-set)
   :variableDefinitions (mapv variable->js variables)})

(defn- operation-definition->js
  "Transform a GraphQL operation type definition from Alumbra to JS."
  [{:keys [alumbra/metadata alumbra/operation-type alumbra/schema-type]}]
  {:kind "OperationTypeDefinition"
   :loc (meatadata->loc metadata)
   :operation operation-type
   :type (named-type->js schema-type)})

(defn- schema-definition->js
  "Transform a GraphQL schema definition from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/schema-fields]}]
  {:kind "SchemaDefinition"
   :loc (meatadata->loc metadata)
   :directives (mapv directive->js directives)
   :operationTypes (mapv operation-definition->js schema-fields)})

(defn- schema-extension->js
  "Transform a GraphQL schema extension from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/schema-fields]}]
  {:kind "SchemaExtension"
   :loc (meatadata->loc metadata)
   :directives (mapv directive->js directives)
   :operationTypes (mapv operation-definition->js schema-fields)})

(defn- enum-value-definition->js
  "Transform a GraphQL enum value from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/enum alumbra/metadata]}]
  {:description nil
   :directives (mapv directive->js directives)
   :kind "EnumValueDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js enum metadata)})

(defn- enum-definition->js
  "Transform a GraphQL enum definition from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/enum-fields alumbra/metadata alumbra/type-name]}]
  {:description nil
   :directives (mapv directive->js directives)
   :kind "EnumTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)
   :values (mapv enum-value-definition->js enum-fields)})

(defn- enum-extension->js
  "Transform a GraphQL enum extension from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/enum-fields alumbra/metadata alumbra/type-name]}]
  {:directives (mapv directive->js directives)
   :kind "EnumTypeExtension"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)
   :values (mapv enum-value-definition->js enum-fields)})

(defn- directive-location->js
  "Transform a GraphQL directive definition from Alumbra to JS."
  [location metadata]
  (-> location name str/upper-case (str/replace "-" "_")
      (name->js metadata)))

(defn- directive-definition->js
  "Transform a GraphQL directive definition from Alumbra to JS."
  [{:keys [alumbra/argument-definitions
           alumbra/directive-name
           alumbra/directive-locations
           alumbra/metadata]}]
  {:arguments (mapv argument-definition->js argument-definitions)
   :description nil
   :kind "DirectiveDefinition"
   :loc (meatadata->loc metadata)
   :locations (mapv #(directive-location->js % metadata) directive-locations)
   :name (name->js directive-name metadata)})

(defn- scalar-definition->js
  "Transform a GraphQL scalar definition from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/type-name]}]
  {:description nil
   :directives (mapv directive->js directives)
   :kind "ScalarTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- scalar-extension->js
  "Transform a GraphQL scalar extension from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/type-name]}]
  {:directives (mapv directive->js directives)
   :kind "ScalarTypeExtension"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)})

(defn- union-definition->js
  "Transform a GraphQL union definition from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/type-name alumbra/union-types]}]
  {:description nil
   :directives (mapv directive->js directives)
   :kind "UnionTypeDefinition"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)
   :types (mapv named-type->js union-types)})

(defn- union-extension->js
  "Transform a GraphQL union extension from Alumbra to JS."
  [{:keys [alumbra/directives alumbra/metadata alumbra/type-name alumbra/union-types]}]
  {:directives (mapv directive->js directives)
   :kind "UnionTypeExtension"
   :loc (meatadata->loc metadata)
   :name (name->js type-name metadata)
   :types (mapv named-type->js union-types)})

(defn alumbra->js
  "Transform a GraphQL document from Alumbra to JS."
  [{:keys [alumbra/enum-definitions
           alumbra/directive-definitions
           alumbra/enum-extensions
           alumbra/fragments
           alumbra/input-extensions
           alumbra/input-type-definitions
           alumbra/interface-definitions
           alumbra/interface-extensions
           alumbra/metadata
           alumbra/operations
           alumbra/scalar-definitions
           alumbra/scalar-extensions
           alumbra/schema-definitions
           alumbra/schema-extensions
           alumbra/type-definitions
           alumbra/type-extensions
           alumbra/union-definitions
           alumbra/union-extensions]}]
  {:definitions
   (vec (concat (map schema-definition->js schema-definitions)
                (map schema-extension->js schema-extensions)
                (map scalar-definition->js scalar-definitions)
                (map scalar-extension->js scalar-extensions)
                (map enum-definition->js enum-definitions)
                (map enum-extension->js enum-extensions)
                (map directive-definition->js directive-definitions)
                (map input-type-definition->js input-type-definitions)
                (map input-extension->js input-extensions)
                (map interface-definition->js interface-definitions)
                (map interface-extension->js interface-extensions)
                (map type-definition->js type-definitions)
                (map type-extension->js type-extensions)
                (map union-definition->js union-definitions)
                (map union-extension->js union-extensions)
                (map operation->js operations)
                (map fragment->js fragments)))
   :kind "Document"
   :loc (meatadata->loc metadata)})
