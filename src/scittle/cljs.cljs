(ns scittle.cljs
  (:require [cljs.reader :refer [read-string]]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def rns (sci/create-ns 'cljs.reader nil))

(def cljs-reader-namespace
  {'read-string (sci/copy-var read-string rns)})

(scittle/register-plugin!
 ::ajax
 {:namespaces {'cljs.reader cljs-reader-namespace}})
