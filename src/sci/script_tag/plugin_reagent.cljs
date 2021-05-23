(ns sci.script-tag.plugin-reagent
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [sci.core :as sci]
            [sci.script-tag :as st]))

(def rns (sci/create-ns 'reagent.core nil))

(def reagent-namespace
  {'atom (sci/copy-var r/atom rns)})

(def rdns (sci/create-ns 'reagent.dom nil))

(def reagent-dom-namespace
  {'render (sci/copy-var rdom/render rdns)})

(println :merging)
(st/merge-ctx {:namespaces {'reagent.core reagent-namespace
                            'reagent.dom reagent-dom-namespace}})
(println :done-merging)
