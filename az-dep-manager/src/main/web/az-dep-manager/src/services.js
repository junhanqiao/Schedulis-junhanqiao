import './mock/mock.js'
import axios from 'axios'
import './config/axios'
function searchProjectByName(searchText, sucCallBack, failCallBack) {
    let _searchText = searchText ? searchText.trim() : '.*'
    let config={
        params: {
            searchText: _searchText,
            ajax: 'searchProjectByName' 
        }        
    }
    axios.get('/dep', config).then(sucCallBack).catch(failCallBack)
}

function searchUserProjectByName(searchText, sucCallBack, failCallBack) {
    let _searchText = searchText ? searchText.trim() : '.*'
    let config={
        params: {
            searchText: _searchText ,
            ajax: 'searchUserProjectByName'

        }        
    }
    axios.get('/dep', config).then(sucCallBack).catch(failCallBack)
}

function getFlowsByProject(projectId, sucCallBack, failCallBack) {
    let config={
        params: {
            projectId,
            ajax:'getFlowsByProject'
        }        
    }
    axios.get('/dep',config).then(sucCallBack).catch(failCallBack)
}

function searchDepRelations(params, sucCallBack, failCallBack) {
    let config={
        params: {
            ...params,
            ajax:'searchFlowRelation'
        }        
    }    
    axios.get('/dep',config).then(sucCallBack).catch(failCallBack)
}

function addFlowRelation(data, sucCallBack, failCallBack) {
    let config={
        params:{
            ajax:'addFlowRelation'
        }
    }    
    axios.post('/dep',data,config).then(sucCallBack).catch(failCallBack)
}
function deleteFlowRelation(id, sucCallBack, failCallBack) {
    let config={
        params:{
            ajax:'deleteFlowRelation',
            id
        }
    }    
    axios.post('/dep',{},config).then(sucCallBack).catch(failCallBack)
}

function searchFlowInstance(params, sucCallBack, failCallBack) {
    let config={
        params: {
            ...params,
            ajax:'searchFlowInstance'
        }        
    }    
    axios.get('/dep',config).then(sucCallBack).catch(failCallBack)
}

function redoFlowInstance(data, sucCallBack, failCallBack) {
    let config={
        params:{
            ajax:'redoFlowInstance'
        }
    }    
    axios.post('/dep',data,config).then(sucCallBack).catch(failCallBack)
}

export default {
    searchProjectByName,
    searchUserProjectByName,
    searchDepRelations,
    getFlowsByProject,
    addFlowRelation,
    deleteFlowRelation,
    searchFlowInstance,
    redoFlowInstance,
}