(defproject r0man/alumbra.js "0.1.1-SNAPSHOT"
  :description "Transformations between Alumbra and JavaScript GraphQL ASTs."
  :url "https://github.com/r0man/alumbra.js"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2018
            :key "mit"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [r0man/alumbra.printer "0.1.0"]]
  :plugins [[jonase/eastwood "0.3.4"]
            [lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.11"]]
  :aliases
  {"ci" ["do"
         ["clean"]
         ["test"]
         ["doo" "node" "node" "once"]
         ["lint"]]
   "lint" ["do"  ["eastwood"]]}
  :cljsbuild
  {:builds
   [{:id "node"
     :compiler
     {:main alumbra.js.runner
      :npm-deps {:graphql "14.0.2"}
      :install-deps true
      :optimizations :none
      :output-dir "target/node"
      :output-to "target/node.js"
      :parallel-build true
      :pretty-print true
      :target :nodejs
      :verbose false}
     :source-paths ["src" "test"]}]}
  :profiles
  {:dev
   {:dependencies [[alumbra/analyzer "0.1.17"]
                   [alumbra/generators "0.2.2"]
                   [alumbra/parser "0.1.7"]
                   [com.gfredericks/test.chuck "0.2.9"]
                   [criterium "0.4.4"]
                   [r0man/alumbra.spec "0.1.11"]
                   [org.clojure/test.check "0.9.0"]]}
   :provided
   {:dependencies [[org.clojure/clojurescript "1.10.439"]]}
   :repl
   {:dependencies [[cider/piggieback "0.3.10"]]
    :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}})
