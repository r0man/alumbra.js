(ns alumbra.js.ast-test
  (:require [alumbra.js.ast :as ast]
            [alumbra.js.examples :as examples]
            [alumbra.js.test :as test]
            [clojure.test :refer [deftest is testing]]
            [graphql :as graphql]))

(defn- parse-str [doc]
  (-> (.parse graphql doc)
      (js->clj :keywordize-keys true)))

(defn- roundtrip? [doc]
  (let [ast (parse-str doc)
        ast' (ast/alumbra->js (ast/js->alumbra ast))]
    (= (test/strip-loc ast) (test/strip-loc ast'))))

(deftest test-examples-roundtrip
  (doseq [[name doc] (examples/all)]
    (testing (str "Document " name)
      (is (roundtrip? doc)))))
