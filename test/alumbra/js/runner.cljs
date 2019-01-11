(ns alumbra.js.runner
  (:require [alumbra.js.ast-test]
            [clojure.spec.test.alpha :as stest]
            [doo.runner :refer-macros [doo-tests]]))

(stest/instrument)

(doo-tests 'alumbra.js.ast-test)
