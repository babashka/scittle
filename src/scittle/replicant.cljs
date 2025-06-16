(ns scittle.replicant
  (:require
   [sci.configs.replicant.replicant-dom :refer [replicant-dom-namespace]]
   [scittle.core :as scittle]))

(scittle/register-plugin!
 ::replicant
 {:namespaces {'replicant.dom replicant-dom-namespace}})
