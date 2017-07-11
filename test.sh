#!/bin/sh
#lein cljsbuild test
pushd ./src
planck ./mccarthy_animation/lispm_test.cljs
#planck ./mccarthy_animation/character_test.cljs
popd
