(ns scittle.dataspex
  {:no-doc true}
  (:require [sci.configs.cjohansen.dataspex :refer [config]]
            [scittle.core :as scittle]))

(defn init []
  (scittle/register-plugin!
   ::dataspex
   config))
