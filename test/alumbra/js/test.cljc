(ns alumbra.js.test
  (:require #?(:clj [alumbra.analyzer :as analyzer])
            #?(:clj [alumbra.generators :as alumbra-gen])
            #?(:clj [alumbra.parser :as parser])
            #?(:clj [alumbra.spec])
            [clojure.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.walk :as walk]))

(defn strip-keys [m ks]
  (let [ks (set ks)
        f (fn [[k v]]
            (when-not (contains? ks k)
              [k v]))]
    (walk/postwalk
     (fn [x]
       (if (map? x)
         (into {} (map f x))
         x))
     m)))

(defn strip-loc [m]
  (strip-keys m #{:loc}))

(defn pprint [x]
  (binding [*print-namespace-maps* false]
    (pprint/pprint x)))

(defn infinity? [x]
  (cond
    (double? x)
    #?(:clj (Double/isInfinite x)
       :cljs (not (js/isFinite x)))
    (float? x)
    #?(:clj (Float/isInfinite x)
       :cljs (not (js/isFinite x)))
    :else false))

(defn contains-infinity? [x]
  (cond
    (infinity? x)
    true
    (map? x)
    (or (some true? (map contains-infinity? (keys x)))
        (some true? (map contains-infinity? (vals x))))
    (sequential? x)
    (some true? (map contains-infinity? x))
    :else x))

(def schema
  "type Person { name: String!, pets: [Pet!] }
   type Pet { name: String!, meows: Boolean }
   union PersonOrPet = Person | Pet
   enum PositionKind { LONG, LAT }
   input Position { x: Int, y: Int, k: PositionKind! }
   type QueryRoot { person(name: String!): Person, random(seed: Position!): PersonOrPet }
   type MutationRoot { createPerson(name: String!): Person! }
   schema { query: QueryRoot, mutation: MutationRoot }")

#?(:clj (def gen-operation
          (alumbra-gen/operation (analyzer/analyze-schema schema parser/parse-schema))))

#?(:clj (defn gen-document []
          (gen/let [operation-name (s/gen :alumbra/operation-name)
                    operation (gen/elements [:query :mutation])
                    document (gen-operation operation operation-name)]
            document)))

#?(:clj (defn- parse-fn! [parse-fn doc throw?]
          (let [ast (parse-fn doc)]
            (if (contains? ast :alumbra/parser-errors)
              (when throw? (throw (ex-info "Can't parse document." ast)))
              ast))))

#?(:clj (defn parse-document! [doc]
          (parse-fn! parser/parse-document doc true)))

#?(:clj (defn parse-schema! [doc]
          (parse-fn! parser/parse-schema doc true)))

#?(:clj (defn parse! [doc]
          (or (parse-fn! parser/parse-schema doc false)
              (parse-fn! parser/parse-document doc false)
              (throw (ex-info "Can't parse schema, nor document." {})))))

#?(:clj (defn gen-raw-document []
          (gen/such-that
           #(not (contains-infinity? (parse-document! %)))
           (alumbra-gen/raw-document) 1000)))

#?(:clj (defn gen-raw-schema []
          (gen/such-that
           #(not (contains-infinity? (parse-schema! %)))
           (alumbra-gen/raw-schema) 1000)))
