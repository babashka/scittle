(ns scittle.reagent
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def rns (sci/create-ns 'reagent.core nil))

(def reagent-namespace
  {'atom (sci/copy-var r/atom rns)
   'cursor (sci/copy-var r/cursor rns)})

(def rdns (sci/create-ns 'reagent.dom nil))

(def reagent-dom-namespace
  {'render (sci/copy-var rdom/render rdns)})

(scittle/register-plugin!
 ::reagent
 {:namespaces {'reagent.core reagent-namespace
               'reagent.dom reagent-dom-namespace}})
