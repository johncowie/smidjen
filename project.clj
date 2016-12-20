(defproject smidje "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:dependencies             [[org.clojure/clojurescript "1.9.293"]]
                   :plugins                  [[com.jakemccrary/lein-test-refresh "0.18.0"]
                                              [lein-doo "0.1.7"]]
                   :test-paths               ["test/clj" "test/cljc"]
                   :cljsbuild                {:builds {:test {:source-paths ["src/" "test/cljc"]
                                                              :compiler     {:output-to     "target/cljs/testable.js"
                                                                             :main          smidje.test-runner
                                                                             :optimizations :whitespace}
                                                              }}}

                   :aliases                  {"test-cljs" ["doo" "phantom" "test" "once"]
                                              "auto-cljs" ["doo" "phantom" "test" "auto"]
                                              ;"test"      ["run" "-m" "smidje.core-test"]
                                              }
                   :monkeypatch-clojure-test false
                   }})
