(ns smidje.test-runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [smidje.core-test]
            [smidje.sample-test]))

(enable-console-print!)

(doo-tests 'smidje.core-test
           ;'smidje.sample-test
           )
