(ns scittle.makros)

#?(:cljs ;only cljs!!
   (defn make-string [& vs]
     (apply str "_cljs!_ " vs)))

#?(:clj
   (defmacro add-slash-makro [x y]
     `(#'make-string "/ form: " ~(str &form) " env: " ~(str (:name (:ns &env))) " params: " ~x ~y)))

;; needs to be both clj and cljs
(defn add-c-fn [form env x y]
  `(#'make-string "c_form: " ~(str form) " c_env: " ~(str (:name (:ns env))) " parms "  ~x ~y))

#?(:clj
   (defmacro add-c-makro [x y]
     (add-c-fn &form &env x y)))
