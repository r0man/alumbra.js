(ns alumbra.js.examples
  #?(:cljs (:require-macros [alumbra.js.examples :refer [defexample]])))

(defonce registry (atom {}))

(defn all
  "Returns all examples."
  []
  @registry)

#?(:clj (defmacro defexample [example-sym document]
          (assert (symbol? example-sym))
          (assert (string? document))
          `(do (def ~example-sym ~document)
               (swap! registry assoc ~(keyword example-sym) ~example-sym)
               nil)))


;; Alias

(defexample aliases
  "{
     empireHero: hero(episode: EMPIRE) {
       name
     }
     jediHero: hero(episode: JEDI) {
       name
     }
   }")

;; Arguments

(defexample arguments-field
  "{
     human(id: \"1000\") {
       name
       height(unit: FOOT)
     }
   }")

;; (defexample arguments-selection-set
;;   "{
;;      human(id: \"1000\") {
;;        name
;;        height
;;      }
;;    }")

;; (defexample arguments-type
;;   "type Starship {
;;      id: ID!
;;      name: String!
;;      length(unit: LengthUnit = METER): Float
;;    }")

;; ;; Default variables

;; (defexample default-variables
;;   "query HeroNameAndFriends($episode: Episode = JEDI) {
;;      hero(episode: $episode) {
;;        name
;;        friends {
;;          name
;;        }
;;      }
;;    }")

;; ;; Directives

;; (defexample directives
;;   "query Hero($episode: Episode, $withFriends: Boolean!) {
;;      hero(episode: $episode) {
;;        name
;;        friends @include(if: $withFriends) {
;;          name
;;        }
;;      }
;;    }")

;; (defexample directives-flat
;;   "{
;;      human {
;;        name @client
;;        height @client
;;      }
;;    }")

;; ;; Enums

;; (defexample enums
;;   "enum Direction {
;;      NORTH
;;      EAST
;;      SOUTH
;;      WEST
;;    }")

;; (defexample enum-extension
;;   "extend enum Direction {
;;      NORTH_EAST
;;    }")

;; ;; Fields

;; (defexample field-simple
;;   "{
;;      hero {
;;        name
;;      }
;;    }")

;; (defexample field-object
;;   "{
;;      hero {
;;        name
;;        # Queries can have comments!
;;        friends {
;;          name
;;        }
;;      }
;;    }")

;; (defexample hero-name-and-friends
;;   "query HeroNameAndFriends {
;;      hero {
;;        name
;;        friends {
;;          name
;;        }
;;      }
;;    }")

;; ;; Fragments

;; (defexample fragments
;;   "{
;;      leftComparison: hero(episode: EMPIRE) {
;;        ...comparisonFields
;;      }
;;      rightComparison: hero(episode: JEDI) {
;;        ...comparisonFields
;;      }
;;    }

;;    fragment comparisonFields on Character {
;;      name
;;      appearsIn
;;      friends {
;;        name
;;      }
;;    }")

;; (defexample fragments-variables
;;   "query HeroComparison($first: Int = 3) {
;;      leftComparison: hero(episode: EMPIRE) {
;;        ...comparisonFields
;;      }
;;      rightComparison: hero(episode: JEDI) {
;;        ...comparisonFields
;;      }
;;    }

;;    fragment comparisonFields on Character {
;;      name
;;      friendsConnection(first: $first) {
;;        totalCount
;;        edges {
;;          node {
;;            name
;;          }
;;        }
;;      }
;;    }")

;; ;; Interface Type

;; (defexample interface-definition
;;   "interface NamedEntity {
;;      name: String
;;    }")

;; (defexample interface-implements
;;   "type Person implements NamedEntity {
;;      name: String
;;      age: Int
;;    }")

;; ;; Interface Type Extension

;; (defexample interface-type-extension
;;   "extend interface NamedEntity {
;;      nickname: String
;;    }")

;; ;; Inline Fragments

;; (defexample inline-fragments
;;   "query HeroForEpisode($ep: Episode!) {
;;      hero(episode: $ep) {
;;        name
;;        ... on Droid {
;;          primaryFunction
;;        }
;;        ... on Human {
;;          height
;;        }
;;      }
;;    }")

;; ;; Input Values

;; (defexample input-values
;;   "{
;;      int_value(input: 1)
;;      float_value(input: 1.2)
;;      boolean_value(input: true)
;;      string_value(input: \"x\")
;;      null_value(input: null)
;;      enum_value(input: ENUM_X)
;;      list_value(input: [1, 2, 3])
;;      object_value(input: {a: 1, b: 2})
;;      object_nested_value(input: {a: 1, b: {c: 2} })
;;    }")

;; ;; Input Objects

;; (defexample input-object-type-definition
;;   "input Point2D {
;;      x: Float
;;      y: Float
;;    }")

;; (defexample input-object-type-extension
;;   "extend input NamedEntity {
;;      nickname: String
;;    }")

;; ;; Meta Fields

;; (defexample meta-fields
;;   "{
;;      search(text: \"an\") {
;;        __typename
;;        ... on Human {
;;          name
;;        }
;;        ... on Droid {
;;          name
;;        }
;;        ... on Starship {
;;          name
;;        }
;;      }
;;    }")

;; ;; Mutations

;; (defexample mutation
;;   "mutation CreateReviewForEpisode($ep: Episode!, $review: ReviewInput!) {
;;      createReview(episode: $ep, review: $review) {
;;        stars
;;        commentary
;;      }
;;    }")

;; ;; Object Type Extension

;; (defexample object-type-definition
;;   "type Person implements NamedEntity {
;;      name: String
;;      age: Int
;;    }")

;; (defexample object-type-extension
;;   "extend type Story {
;;      isHiddenLocally: Boolean
;;    }")

;; ;; Operation name

(defexample operation-name
  "query HeroNameAndFriends {
     hero {
       name
       friends {
         name
       }
     }
   }")

;; Scalars

(defexample scalars
  "scalar Time
   scalar Url")

(defexample scalar-type-extension
  "extend scalar Url @example")

;; Schema Definitions

(defexample schema-definition
  "schema {
     query: MyQueryRootType
     mutation: MyMutationRootType
   }")

(defexample schema-extension
  "extend schema {
     query: MyQueryRootType
     mutation: MyMutationRootType
   }")

(defexample schema-definitions
  "schema {
     query: MyQueryRootType
     mutation: MyMutationRootType
   }

   type MyQueryRootType {
     someField: String
   }

   type MyMutationRootType {
     setSomeField(to: String): String
   }")

;; Unions

(defexample union-search-result
  "union SearchResult = Photo | Person")

(defexample union-type-extension
  "extend union SearchResult = Photo | Person")

;; Variables

(defexample variables
  "query HeroNameAndFriends($episode: Episode) {
     hero(episode: $episode) {
       name
       friends {
         name
       }
     }
   }")

(defexample variable-types
  "query Types(
     $boolean: Boolean,
     $enum: MY_ENUM,
     $float: Float,
     $int: Int,
     $list_type: [Int],
     $object: MyObject,
     $string: String
   ) {
     types(
       boolean: $boolean,
       enum: $enum,
       float: $float,
       int: $int,
       list_type: $list_type,
       object: $object,
       string: $string
     ) {
       id
     }
   }")

(defexample variable-types-non-null
  "query TypesNonNull(
     $boolean: Boolean!,
     $enum: MY_ENUM!,
     $float: Float!,
     $int: Int!,
     $list_type: [Int!]!,
     $object: MyObject!,
     $string: String!
   ) {
     types(
       boolean: $boolean,
       enum: $enum,
       float: $float,
       int: $int,
       list_type: $list_type,
       object: $object,
       string: $string
     ) {
       id
     }
   }")

(defexample variable-list-type-nullable
  "query Search($ids: [ID!]) {
     search(ids: $ids) {
       edges {
         node {
           id
         }
       }
     }
   }")

(defexample variable-list-type-non-nullable
  "query Search($ids: [ID!]!) {
     search(ids: $ids) {
       edges {
         node {
           id
         }
       }
     }
   }")
