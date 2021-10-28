import './mock/mock.js'
import axios from 'axios'
import './config/axios'

function searchProjectByName(searchText,sucCallBack,failCallBack){
    axios.get('/project/searchProject').then(sucCallBack).catch(failCallBack)
}

function searchDepRelations(params,sucCallBack,failCallBack){
    axios.get('/dep/relations').then(sucCallBack).catch(failCallBack)
}

export default {
    searchProjectByName,
    searchDepRelations,
}