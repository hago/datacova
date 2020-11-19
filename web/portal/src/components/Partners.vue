<template>
  <div>
    <button v-on:click="newPartner" class="btn btn-primary">New Partner</button>
    <div class="row" v-for="partner of partners" :key="partner.id">
      <div class="col-2 jobtitle">
        <a href="javascript:void(0)" v-on:click="editPartner" :data-id="partner.id">{{ partner.name }}</a>
      </div>
      <div class="col-3"></div>
      <div class="col-1">{{ partner.addBy }}</div>
      <div class="col-2 addtime">{{ dateFormat(new Date(partner.addTime), "isoDateTime") }}</div>
      <div class="col-2" v-if="partner.status == 0">Normal</div>
      <div class="col-2" v-else-if="partner.status == 1">Disabled</div>
      <div class="col-2" v-if="partner.status == 0">
        <button class="btn btn-info" v-on:click="toggle" :data-id="partner.id">Disable</button>
      </div>
      <div class="col-2" v-if="partner.status == 1">
        <button class="btn btn-info" v-on:click="toggle" :data-id="partner.id">Enable</button>
      </div>
    </div>
  </div>
</template>

<script>
import PartnerApiHelper from '../apis/partnerapi'
import Toasted from 'vue-toasted'
import Vue from 'vue'
import User from '../apis/user.js'
const dateFormat = require('dateformat')

Vue.use(Toasted)

export default {
  name: 'Partners',
  data () {
    return {
      partners: []
    }
  },
  created: function () {
    (new User()).checkLogin(true, this.loadPartners)
  },
  methods: {
    loadPartners: function () {
      (new PartnerApiHelper(process.env.SERVICE_BASE_URL)).loadPartners()
        .then(rsp => {
          this.partners = rsp.data.data.partners
        })
        .catch(error => {
          Vue.toasted.show(error)
        })
    },
    newPartner: function () {
      this.$router.push({name: 'PartnerEdit', params: { id: -1 }})
    },
    editPartner: function (event) {
      this.$router.push({name: 'PartnerEdit', params: { id: event.target.getAttribute('data-id') }})
    },
    dateFormat: function (date, fmt) {
      return dateFormat(date, fmt)
    },
    toggle: function (event) {
      let id = parseInt(event.target.getAttribute('data-id'));
      (new PartnerApiHelper(process.env.SERVICE_BASE_URL)).togglePartner(id)
        .then(rsp => {
          for (let i in this.partners) {
            let p = this.partners[i]
            if (p.id === id) {
              p.status = Math.abs(p.status - 1)
              Vue.set(this.partners, i, p)
              break
            }
          }
        })
        .catch(error => {
          Vue.toasted.show(error)
        })
    }
  }
}
</script>

<style scoped>

</style>
