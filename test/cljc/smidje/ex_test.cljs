(ns smidje.ex-test
  (:require
    [cljs.test :include-macros true]
    [smidje.core :refer [=> =not=>] :refer-macros [fact facts]]
    [smidje.debug :refer-macros [cljs-macroexpand-all debug-all->js debug-all->cons]]))

(fact "basic_int_test"
      1 => 2
      (fact "about_some_other_stuff"
            :a => :b
            (* 7 8) => 42))

(fact "string-test"
      (* 3 3) => 10
      :a => :c
      (fact "another*test"
            :d => :e))

;; FIXME issues
;;  Can't have whitespace in symbol names
;;  Nesting doesn't work?

