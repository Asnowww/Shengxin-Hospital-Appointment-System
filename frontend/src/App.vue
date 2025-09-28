<script setup>
import { RouterView } from 'vue-router'
import bgImage from '@/assets/images/bg.jpg'
</script>

<template>
  <div class="bg-image" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <RouterView />
</template>


<style>
body {
  margin: 0;
  font-family: Arial, sans-serif;
}
.bg-image {
  position: fixed;
  top: 0; left: 0;
  width: 100vw;
  height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.15;
  z-index: -1;
  pointer-events: none;
  user-select: none;
}
</style>
