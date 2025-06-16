(ns scittle.replicant
  (:require
   [sci.configs.cjohansen.replicant :refer [config]]
   [scittle.core :as scittle]))

(scittle/register-plugin!
 ::replicant
 config)
