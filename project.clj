(defproject smidje "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]]
  :profiles {:dev     {:plugins                  [[com.jakemccrary/lein-test-refresh "0.18.0"]
                                                  [lein-doo "0.1.7"]]
                       :test-paths               ["test/clj"]
                       ;:cljsbuild                {:builds {:test {:source-paths ["src/" "test/cljc" "test/cljs"]
                       ;                                           :compiler     {:output-to     "target/cljs/testable.js"
                       ;                                                          :main          smidje.test-runner
                       ;                                                          :optimizations :whitespace}}}}
                       :monkeypatch-clojure-test false}
             :example {:plugins                  [[com.jakemccrary/lein-test-refresh "0.18.0"]
                                                  [lein-doo "0.1.7"]]
                       :source-paths             ["src/"]
                       :test-paths               ["example/cljc"]
                       :cljsbuild                {:builds {:test {:source-paths ["src/" "example/cljc" "example/cljs"]
                                                                  :compiler     {:output-to     "target/cljs/testable.js"
                                                                                 :main          smidje.test-runner
                                                                                 :optimizations :whitespace}}}}
                       :monkeypatch-clojure-test false}}
  :aliases {
            ;"test-cljs"    ["with-profile" "dev" "doo" "phantom" "test" "once"]
            ;"auto-cljs"    ["with-profile" "dev" "doo" "phantom" "test" "auto"]
            "example"      ["with-profile" "example" "test" "smidje.example-test"]
            "example-cljs" ["with-profile" "example" "doo" "phantom" "test" "once"]}
  )
