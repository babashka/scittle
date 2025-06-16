(ns scittle.reitit
  (:require [sci.configs.metosin.reitit :refer [config]]
            [scittle.core :as scittle]))

(scittle/register-plugin!
 ::reitit
 config)
  