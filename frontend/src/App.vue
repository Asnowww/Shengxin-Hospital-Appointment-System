<template>
  <div id="app">
    <Navigation />

    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import Navigation from '@/components/Navigation.vue'
</script>

<style>
/* App.vue（或全局样式） */
:root { --nav-h: 82px; }

html,body,#app {
  height: 100%;
  margin: 0;
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding-top: var(--nav-h);   /* 把导航高度留给内容区域 */
  box-sizing: border-box;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
  /* 可选：保证 content 的可视高度不超出 */
  height: calc(100vh - var(--nav-h));
}


</style>
