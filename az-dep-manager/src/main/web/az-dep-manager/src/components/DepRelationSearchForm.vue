<template>
 <div id="dep-relation-search-form">
    <a-form class="dep-relation-add-form" :form="form"  @submit="handleSearch">
      <a-row>
        <a-col :key="'depended_project_id'">
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
        <a-col :key="'depended_flow_id'">
          <a-form-item :label="`前置工作流`">
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
              v-decorator="['depended_flow_id']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
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
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        console.log('error', error);
        console.log('Received values of form: ', values);
      });
      let params={}

       services.searchDepRelations(
         params,
         res=>{this.searchCallBack(res.data)}
        )
    },

    handleReset() {
      this.form.resetFields();
    },
  }
};
</script>
