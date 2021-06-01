(ns scittle.ajax
  (:require [ajax.core :as ajx]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def rns (sci/create-ns 'ajax.core nil))

(def ajax-namespace
  {'GET (sci/copy-var ajx/GET rns)
   'POST (sci/copy-var ajx/POST rns)})

(scittle/register-plugin!
 ::ajax
 {:namespaces {'ajax.core ajax-namespace}})
