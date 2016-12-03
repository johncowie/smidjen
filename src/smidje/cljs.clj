(ns smidje.cljs
  (:require [smidje.core :refer [expand-facts future-fact-expr]]))

(declare fact facts future-fact future-facts =not=> =>)

(defmacro fact [& body]
  (expand-facts 'cljs.test 'smidje.cljs body))

(defmacro facts [& body]
  (expand-facts 'cljs.test 'smidje.cljs body))

(defmacro future-fact [& body]
  (future-fact-expr body))

(defmacro future-facts [& body]
  (future-fact-expr body))
