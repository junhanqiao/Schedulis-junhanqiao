<template>
 <div id="dep-relation-search-form">
    <a-form class="dep-relation-add-form" :form="form"  @submit="handleSearch">
      <a-row>
        <a-col :key="'depended_project_id'" :span="4">
          <a-form-item :label="`前置项目`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['depended_project_id']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'depended_flow_id'" :span="4">
          <a-form-item :label="`前置工作流`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedFlowSearch"
              v-decorator="['depended_flow_id']"
            >
              <a-select-option v-for="d in depedFlows" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row>
        <a-col :key="'project_id'" :span="4">
          <a-form-item :label="`项目`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['project_id']"
            >
              <a-select-option v-for="d in projects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'flow_id'" :span="4">
          <a-form-item :label="`工作流`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleFlowSearch"
              v-decorator="['flow_id']"
            >
              <a-select-option v-for="d in flows" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row>
        <a-col :span="24" :style="{ textAlign: 'right' }">
          <a-button type="primary" html-type="submit">
            查询
          </a-button>
          <a-button :style="{ marginLeft: '8px' }" @click="handleReset">
            重置
          </a-button>      
        </a-col>
      </a-row>      
    </a-form>
    <slot></slot>
    <a-pagination
      show-size-changer
      :default-current="1"
      :pageSize="pageSize"
      :current="pageNum"
      :total="total"
      @change="handlePageChange"
      @showSizeChange="handleShowSizeChange"
    />    
 </div>
</template>
<script>
import services from "../services";
export default {
  name: "DepRelationSearchForm",
  props:{
      searchCallBack:Function,
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      depedProjects: [],
      depedFlows: [],
      projects: [],
      flows: [],
      pageSize:20,
      pageNum:1,
      total:100,
    };
  },
  computed: {},
  methods: {
    handleDepedProjectSearch(value) {
      services.searchProjectByName(value, res => {
        this.depedProjects = res.data;
      });
    },
    handleDepedProjectChange(value, option) {
      console.log(value);
      console.log(option);
      //update depedFlows
    },
    handleDepedFlowSearch(value) {
      console.log(value);
    },
    handleProjectSearch(value) {
      services.searchProjectByName(value, res => {
        this.depedProjects = res.data;
      });
    },
    handleProjectChange(value, option) {
      console.log(value);
      console.log(option);
      //update depedFlows
    },
    handleFlowSearch(value) {
      console.log(value);
    },
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        console.log('error', error);
        console.log('Received values of form: ', values);
      });
      let params={}

       services.searchDepRelations(
         params,
         res=>{
           this.searchCallBack(res.data)
        }
        )
    },

    handleReset() {
      this.form.resetFields();
    },
    handlePageChange(page,pageSize){
      console.log({page,pageSize})
      this.pageNum=page
    },
    handleShowSizeChange(page,pageSize){
      console.log({page,pageSize})
      this.pageSize=pageSize
    }

  }
};
</script>
