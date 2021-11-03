<template>
  <div id="dep-relation-search-form">
    <a-form class="dep-relation-add-form" :form="form" @submit="handleSearch">
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
              v-decorator="['dependedProjectId']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.id">{{ d.name }}</a-select-option>
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
              :filter-option="handleDepedFlowSearch"
              :not-found-content="null"
              v-decorator="['dependedFlowId']"
            >
              <a-select-option v-for="flowId in depedFlows" :key="flowId">{{ flowId }}</a-select-option>
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
              @search="handleProjectSearch"
              @change="handleProjectChange"
              v-decorator="['projectId']"
            >
              <a-select-option v-for="d in projects" :key="d.id">{{ d.name }}</a-select-option>
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
              :filter-option="handleFlowSearch"
              :not-found-content="null"
              v-decorator="['flowId']"
            >
              <a-select-option v-for="flowId in flows" :key="flowId">{{ flowId }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row>
        <a-col :span="24" :style="{ textAlign: 'right' }">
          <a-button type="primary" icon="search" html-type="submit">查询</a-button>
          <a-button  @click="handleReset">重置</a-button>
          <a-button type="primary" @click="handleAdd">新增</a-button>
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
    <DepRelationAddForm :visible='addFormVisible' :okCallBack='addOk' :cancelCallBack='addCancel'/>        
  </div>
</template>
<script>
import services from "../services";
import DepRelationAddForm from './DepRelationAddForm'
export default {
  name: "DepRelationSearchForm",
  components:{DepRelationAddForm},
  props: {
    searchCallBack: Function
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      depedProjects: [],
      depedFlows: [],
      projects: [],
      flows: [],
      pageSize: 20,
      pageNum: 1,
      total: 100,
      //add form
      addFormVisible:false,
    };
  },
  computed: {},
  methods: {
    handleDepedProjectSearch(value) {
      services.searchProjectByName(
        value,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
          this.depedProjects = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleDepedProjectChange(value, option) {
      //update depedFlows
      services.getFlowsByProject(
        value,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
          this.depedFlows = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },

    handleDepedFlowSearch(value, option) {
      return option.componentOptions.children[0].text.indexOf(value) >= 0;
    },
    handleProjectSearch(value) {
      services.searchUserProjectByName(
        value,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
          this.projects = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleProjectChange(value, option) {
      //update flows
      services.getFlowsByProject(
        value,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
          this.flows = res.data.data;
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleFlowSearch(value, option) {
      return option.componentOptions.children[0].text.indexOf(value) >= 0;
    },
    handleSearch(e) {
      e.preventDefault();
      this.doSearch();
    },

    handleReset() {
      this.form.resetFields();
    },
    handlePageChange(page, pageSize) {
      console.log({ page, pageSize });
      this.pageNum = page;
      this.doSearch();
    },
    handleShowSizeChange(page, pageSize) {
      console.log({ page, pageSize });
      this.pageSize = pageSize;
      this.doSearch();
    },
    doSearch() {
      this.form.validateFields((error, values) => {
        if (error) {
          console.log("error", error);
          return;
        }
      });
      let params = this.form.getFieldsValue();
      let pageSize = this.pageSize;
      let pageNum = this.pageNum;
      params = { ...params, pageSize, pageNum };

      services.searchDepRelations(
        params,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
          this.total=res.data.total
          this.searchCallBack(res.data);
        },
        res => {
          this.$message.error("Error");
        }
      );
    },
    handleAdd() {
      this.addFormVisible=true
    },
    addOk(){
        this.addFormVisible=false
        this.doSearch()
    },
    addCancel(){
        this.addFormVisible=false
    }
  }
};
</script>
