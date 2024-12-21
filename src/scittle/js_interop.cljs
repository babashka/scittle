(ns scittle.js-interop
  (:require
   [sci.configs.applied-science.js-interop :as j]
   [scittle.core :as scittle]))

(scittle/register-plugin!
  ::js-interop
  j/config)
