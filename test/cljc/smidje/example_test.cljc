(ns smidje.example-test
  (:require
    #?(:clj
        [smidje.core :refer [fact facts]]
       :cljs
       [cljs.test :include-macros true]
       ;[cljs.analyzer :as ana]
       [smidje.core  :refer-macros [fact facts]])))

;{:ns {:rename-macros {},
;      :renames {},
;      :use-macros {facts smidje.core, fact smidje.core, smidje.core, => smidje.core, =not=> smidje.core},
;      :excludes #{},
;      :name smidje.example-test,
;      :imports nil,
;      :requires {smidje.core smidje.core},
;      :uses {=> smidje.core, =not=> smidje.core},
;      :require-macros {smidje.core smidje.core},
;      :doc nil},
; :context :statement,
; :locals {},
; :fn-scope [],
; :js-globals {console {:name console},
;              location {:name location},
;              escape {:name escape},
;              screen {:name screen},
;              global {:name global},
;              process {:name process},
;              require {:name require},
;              alert {:name alert},
;              history {:name history},
;              window {:name window},
;              module {:name module},
;              exports {:name exports},
;              document {:name document},
;              navigator {:name navigator},
;              unescape {:name unescape}},
; :build-options {:output-file "smidje/test_runner.js", :output-dir "out",
;                 :ups-libs nil, :optimizations :whitespace,
;                 :ups-foreign-libs nil,
;                 :output-to "target/cljs/testable.js", :preamble ["cljs/imul.js" "cljs/imul.js"],
;                 :ups-externs nil, :main "smidje.test-runner", :emit-constants nil}, :line 6, :column 1}


#_(fact "about basic string equality"
      "derek" => "clive")

;(facts "About number equality"
;       (+ 2 2) => 5)
;
;(facts "about multiple number facts, interrupted by some other code"
;       (prn "test1")
;       (+ 1 2) => 4
;       (prn "test2")
;       5 => (- 7 3))
;
;(facts
;  (+ 1 1) => "bob")
