(ns scittle.reagent
  (:require
   [reagent.dom :as rdom]
   [sci.configs.reagent.reagent :refer [reagent-debug-namespace
                                        reagent-namespace reagent-ratom-namespace]]
   [sci.core :as sci]
   [scittle.core :as scittle]))

(def rdns (sci/create-ns 'reagent.dom nil))

(def reagent-dom-namespace
  {'render (sci/copy-var rdom/render rdns)})

(scittle/register-plugin!
 ::reagent
 {:namespaces {'reagent.core reagent-namespace
               'reagent.dom reagent-dom-namespace
               'reagent.ratom reagent-ratom-namespace
               'reagent.debug reagent-debug-namespace}})
