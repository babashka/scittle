(ns scittle.cljs-devtools
  (:require [devtools.core :as devtools]))

(devtools/set-pref! :disable-advanced-mode-check true)

(devtools/install!)
