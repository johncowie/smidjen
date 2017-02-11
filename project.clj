(defproject smidjen "0.1.0"
  :description "Just a smidjen of midje"
  :url "http://github.com/johncowie/smidjen"
  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  ;; FIXME DON'T INCLUDE THESE AS DEPENDENCIES
  :profiles {:dev     {:plugins                  [[com.jakemccrary/lein-test-refresh "0.18.0"]
                                                  [lein-doo "0.1.7"]]
                       :dependencies             [[org.clojure/clojure "1.8.0"]
                                                  [org.clojure/clojurescript "1.9.293"]]
                       :test-paths               ["test/clj"]
                       ;:cljsbuild                {:builds {:test {:source-paths ["src/" "test/cljc" "test/cljs"]
                       ;                                           :compiler     {:output-to     "target/cljs/testable.js"
                       ;                                                          :main          smidjen.test-runner
                       ;                                                          :optimizations :whitespace}}}}
                       :monkeypatch-clojure-test false}
             :example {:plugins                  [[com.jakemccrary/lein-test-refresh "0.18.0"]
                                                  [lein-doo "0.1.7"]]
                       :dependencies             [[org.clojure/clojure "1.8.0"]
                                                  [org.clojure/clojurescript "1.9.293"]]
                       :source-paths             ["src/"]
                       :test-paths               ["example/cljc"]
                       :cljsbuild                {:builds {:test {:source-paths ["src/" "example/cljc" "example/cljs"]
                                                                  :compiler     {:output-to     "target/cljs/testable.js"
                                                                                 :main          smidje.test-runner
                                                                                 :optimizations :whitespace}}}}
                       :monkeypatch-clojure-test false}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :aliases {
            ;"test-cljs"    ["with-profile" "dev" "doo" "phantom" "test" "once"]
            ;"auto-cljs"    ["with-profile" "dev" "doo" "phantom" "test" "auto"]
            "example"      ["with-profile" "example" "test" "smidjen.example-test"]
            "example-cljs" ["with-profile" "example" "doo" "phantom" "test" "once"]}
  )
