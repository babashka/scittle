(ns scittle.hoplon
  {:no-doc true}
  (:require [sci.configs.hoplon.hoplon :refer [config]]
            [scittle.core :as scittle]))

(defn init []
  (scittle/register-plugin!
   ::hoplon
   config))
