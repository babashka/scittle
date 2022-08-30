(ns scittle.promesa
  (:require [scittle.core :as scittle]
            [sci.configs.funcool.promesa :as p]))

(scittle/register-plugin!
  ::promesa
  p/config)
