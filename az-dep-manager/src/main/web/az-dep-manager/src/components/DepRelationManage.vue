<template>
  <div id="dep-relation-manage">
    <a-button type="primary" @click="handleAdd">
      新增依赖
    </a-button>      
    <DepRelationSearchForm :searchCallBack='searchCallBack'/>

    <div>
        <a-table bordered :data-source="tableData" :columns="columns" :rowKey="`id`">
            <template slot="operation" slot-scope="text, record">
                <a-popconfirm
                v-if="tableData.length"
                title="Sure to delete?"
                @confirm="() => onDelete(record.id)"
                >
                <a href="javascript:;">Delete</a>
                </a-popconfirm>
            </template>
        </a-table>
    </div>
    <DepRelationAddForm :visible='addFormVisible' :okCallBack='addOk' :cancelCallBack='addCancel'/>
  </div>
  
</template>
<script>
import DepRelationAddForm from './DepRelationAddForm'
import DepRelationSearchForm from './DepRelationSearchForm'
export default {
  name: 'DepRelationManage',  
  components:{DepRelationAddForm,DepRelationSearchForm},
  data() {
    return {
      tableData: [],
      columns: [
        {
          title: 'id',
          dataIndex: 'id',
          width: '30%',
          scopedSlots: { customRender: 'id' },
        },
        {
          title: 'depended_project_id',
          dataIndex: 'depended_project_id',
        },
        {
          title: 'depended_flow_id',
          dataIndex: 'depended_flow_id',
        },
        {
          title: 'project_id',
          dataIndex: 'project_id',
        },
        {
          title: 'flow_id',
          dataIndex: 'flow_id',
        },
        {
          title: 'create_time',
          dataIndex: 'create_time',
        },
        {
          title: 'modify_time',
          dataIndex: 'modify_time',
        },
        {
          title: 'operation',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
        },
      ],      

      //add form
      addFormVisible:false,
    };
  },
  computed: {

  },
  methods: {

    searchCallBack(tableData){
        this.tableData=tableData
    },

    onDelete(key) {
      const tableData = [...this.tableData];
      this.tableData = tableData.filter(item => item.id !== key);
    },
    handleAdd() {
      this.addFormVisible=true
    },
    addOk(){
        this.addFormVisible=false
    },
    addCancel(){
        this.addFormVisible=false
    }
  },
};
</script>
