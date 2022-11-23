(ns scittle.cljs-ajax
  (:require [ajax.core]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def ans (sci/create-ns 'ajax.core nil))

(def ajax-namespace
  (sci/copy-ns ajax.core ans))

(scittle/register-plugin!
 ::ajax
 {:namespaces {'ajax.core ajax-namespace}})
