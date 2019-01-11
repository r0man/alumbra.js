# alumbra.js

Transformation functions between Alumbra and JavaScript GraphQL ASTs.

[![Build Status](https://travis-ci.org/r0man/alumbra.js.svg?branch=master)](https://travis-ci.org/r0man/alumbra.js)
[![Clojars Project](https://img.shields.io/clojars/v/r0man/alumbra.js.svg)](https://clojars.org/r0man/alumbra.js)

## Usage

```clojure
(require '[alumbra.js.ast :as ast])
(require '[alumbra.parser :as parser])
(require '[clojure.pprint :refer [pprint]])

(def my-document
  (parser/parse-document "{ human(id: \"1000\") { name height(unit: FOOT) } }"))

;; Convert an Alumbra GraphQL document AST to JavaScript.

(def my-js-ast
  (ast/alumbra->js my-document))

(pprint my-js-ast)

;; {:definitions
;;  [{:directives [],
;;    :kind "OperationDefinition",
;;    :loc {:startToken {:column 0, :line 0, :start 0}},
;;    :name nil,
;;    :operation "query",
;;    :selectionSet
;;    {:kind "SelectionSet",
;;     :selections
;;     [{:alias nil,
;;       :arguments ...

;; Convert a JavaScript GraphQL document AST ton Alumbra.

(def my-alumbra-ast
  (ast/js->alumbra my-js-ast))

(binding [*print-namespace-maps* false]
  (pprint my-alumbra-ast))

;; {:alumbra/metadata {:column 0, :index 0, :row 0},
;;  :alumbra/operations
;;  [{:alumbra/metadata {:column 0, :index 0, :row 0},
;;    :alumbra/operation-type "query",
;;    :alumbra/selection-set
;;    [{:alumbra/field-name "human",
;;      :alumbra/metadata {:column 2, :index 2, :row 0},
;;      :alumbra/arguments ...
```

## License

MIT License

Copyright (c) 2018 Roman Scherer
