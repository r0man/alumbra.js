(ns alumbra.js.ast
  (:require [alumbra.js.ast.alumbra :as alumbra]
            [alumbra.js.ast.javascript :as javascript]))

(defn alumbra->js
  "Transform the Alumbra GraphQL `document` AST to JavaScript."
  [document]
  (alumbra/alumbra->js document))

(defn js->alumbra
  "Transform the JavaScript GraphQL `document` AST to Alumbra."
  [document]
  (javascript/js->alumbra document))
