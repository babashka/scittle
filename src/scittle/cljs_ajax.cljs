(ns scittle.cljs-ajax
  (:require [ajax.core :as ajx]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def ans (sci/create-ns 'ajax.core nil))

(def ajax-namespace
  {'GET (sci/copy-var ajx/GET ans)
   'POST (sci/copy-var ajx/POST ans)})

(scittle/register-plugin!
 ::ajax
 {:namespaces {'ajax.core ajax-namespace}})
