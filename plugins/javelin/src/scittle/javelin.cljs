(ns scittle.javelin
  {:no-doc true}
  (:require [sci.configs.hoplon.javelin :refer [config]]
            [scittle.core :as scittle]))

(defn init []
  (scittle/register-plugin!
   ::javelin
   config))
