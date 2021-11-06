<template>
  <a-layout id="components-layout-demo-top-side-2">
    <a-layout-header class="header">
      <div class="logo" />
      <a-menu
        theme="dark"
        mode="horizontal"
        :default-selected-keys="['2']"
        :style="{ lineHeight: '64px' }"
         @click="handleNavClick"
      >
        <a-menu-item key="homepage">
          首页
        </a-menu-item>
        <a-menu-item key="index">
          项目
        </a-menu-item>
        <a-menu-item key="schedule">
          定时调度
        </a-menu-item>
        <a-menu-item key="dep">
          依赖管理
        </a-menu-item>        
        <a-menu-item key="executor">
          正在运行
        </a-menu-item>
        <a-menu-item key="history">
          执行历史
        </a-menu-item>
        <a-menu-item key="system">
          系统管理
        </a-menu-item>
        <a-sub-menu key="login">
          <span slot="title"><a-icon type="down" /><span>superadmin</span></span>
          <a-menu-item key="1">
            登出
          </a-menu-item>
          <a-menu-item key="2">
            中英文切换
          </a-menu-item>
      </a-sub-menu>        
      </a-menu>
    </a-layout-header>
    <a-layout>
      <a-layout style="padding: 0 24px 24px">
        <a-layout-content
          :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
        >
        <a-tabs
          default-active-key="1"
          :tab-position="`left`"
        >
          <a-tab-pane  :key="1" :tab="`依赖管理`"> 
            <DepRelationManage/>
          </a-tab-pane>
          <a-tab-pane  :key="2" :tab="`执行管理`"> 
            <DepExecManage/>
          </a-tab-pane>
        </a-tabs>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </a-layout>
</template>
<script>
import DepRelationManage from './components/DepRelationManage'
import DepExecManage from './components/DepExecManage'
export default {
  data() {
    return {
      collapsed: false,
    };
  },
  components:{DepRelationManage,DepExecManage},
  methods:{
    handleNavClick(value){
      window.location=value.key
    },

  }
};
</script>

<style>
#components-layout-demo-top-side-2 .logo {
  width: 120px;
  height: 31px;
  background: rgba(255, 255, 255, 0.2);
  margin: 16px 28px 16px 0;
  float: left;
}
</style>
