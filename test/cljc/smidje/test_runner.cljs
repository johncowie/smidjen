(ns smidje.test-runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [smidje.example-test]))

(enable-console-print!)

(doo-tests 'smidje.example-test)
