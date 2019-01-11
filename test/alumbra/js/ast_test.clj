(ns alumbra.js.ast-test
  (:require [alumbra.js.ast :as ast]
            [alumbra.js.test :as test]
            [alumbra.parser :as parser]
            [clojure.test :refer [is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.test.chuck :as chuck]))

(defn- parse [doc parse-fn]
  (let [ast (parse-fn doc)]
    (when-not (contains? ast :alumbra/parser-errors)
      ast)))

(defn- parse-str [doc & [opts]]
  (or (parse doc parser/parse-document)
      (parse doc parser/parse-schema)))

(defn- roundtrip? [doc]
  (let [ast (parse-str doc)
        ast' (ast/js->alumbra (ast/alumbra->js ast))]
    (is (= ast ast'))))

(defspec t-roundtrip-document (chuck/times 20)
  (prop/for-all [document (test/gen-document)]
    (roundtrip? document)))

(defspec t-roundtrip-raw-document (chuck/times 20)
  (prop/for-all
    [document (test/gen-raw-document)]
    (let [ast (parse-str document)]
      (= ast (ast/js->alumbra (ast/alumbra->js ast))))))

(defspec t-roundtrip-raw-schema (chuck/times 20)
  (prop/for-all
    [document (test/gen-raw-schema)]
    (let [ast (parse-str document)
          ast' (ast/js->alumbra (ast/alumbra->js ast))]
      (= ast ast'))))
