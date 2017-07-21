#!/bin/sh

pushd ./src
planck ./mccarthy_animation/core_test.cljs
planck ./mccarthy_animation/lispm_test.cljs
# can't test with quil dependency 
#planck ./mccarthy_animation/character_test.cljs
popd

lein trampoline cljsbuild repl-rhino <<EOF
(require 'mccarthy-animation.tests :reload) 
EOF
exit

