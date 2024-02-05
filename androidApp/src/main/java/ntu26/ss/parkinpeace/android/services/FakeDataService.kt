package ntu26.ss.parkinpeace.android.services

import kotlinx.serialization.json.Json
import ntu26.ss.parkinpeace.android.models.CarparkAvailability

object FakeDataService {
    val nearbyCarparks = Json.decodeFromString<List<CarparkAvailability>>(
        """
    [
      {
        "id": "01HEHPDHP46R1R2C4ZQSDGD259",
        "info": {
          "id": "01HEHPDHP46R1R2C4ZQSDGD259",
          "ref": "ura/K0061",
          "name": "KALLANG BAHRU",
          "address": "KALLANG/WHAMPOA NEW TOWN,",
          "epsg4326": "+01.3186895+103.8661314/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 5,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1020,
              "rate": 0,
              "minDuration": 0,
              "capacity": 5,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 5,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "d51d2008fa8e519379d83d4592ae8c2dcfb56e7744b953d546e383d1ea17ed39"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10618,
        "travelTime": 19,
        "lots": {},
        "asof": "2023-11-06T07:02:13.876781500Z"
      },
      {
        "id": "01HEHPDGKPXDWSNH34RAJHP43V",
        "info": {
          "id": "01HEHPDGKPXDWSNH34RAJHP43V",
          "ref": "hdb/BLM",
          "name": "BLK 10 BENDEMEER ROAD",
          "address": "BENDEMEER LIGHT, 10 BENDEMEER ROAD SINGAPORE 330010",
          "epsg4326": "+01.3155064+103.8609587/",
          "lots": [],
          "features": [],
          "hash": "6493182f37b0546dd76190743aba19951df436bad6e4a3aa1d98e7c7a90dd796"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10779,
        "travelTime": 20,
        "lots": {
          "pip.vehicle_type/car": {
            "c": 421,
            "p": -1
          }
        },
        "asof": "2023-11-06T07:01:00Z"
      },
      {
        "id": "01HEHPDHP70DQ3P8THCPW5QG95",
        "info": {
          "id": "01HEHPDHP70DQ3P8THCPW5QG95",
          "ref": "ura/R0037",
          "name": "RANGOON LANE OFF STREET",
          "address": "KALLANG/WHAMPOA NEW TOWN,",
          "epsg4326": "+01.3165411+103.8541962/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 21,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 21,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 21,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "5124ac965fa6fcb5e6ab20d8eb82063aa01d56908903e14d89f40208bc9cc29a"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10915,
        "travelTime": 20,
        "lots": {},
        "asof": "2023-11-06T07:02:13.925345600Z"
      },
      {
        "id": "01HEHPDHP7SGVGF55MM5GD0V1Q",
        "info": {
          "id": "01HEHPDHP7SGVGF55MM5GD0V1Q",
          "ref": "ura/R0039",
          "name": "RANGOON LANE",
          "address": "8 RANGOON LANE SINGAPORE 218504",
          "epsg4326": "+01.3165217+103.8539446/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 3,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 3,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 3,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "39c01c41bdeb7890da9d845e1ba2f04d3758545381edb1dbf52dc8b12d6eab9b"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10880,
        "travelTime": 19,
        "lots": {},
        "asof": "2023-11-06T07:02:13.949576200Z"
      },
      {
        "id": "01HEHPDHP7W25SX68DCC6FZWEN",
        "info": {
          "id": "01HEHPDHP7W25SX68DCC6FZWEN",
          "ref": "ura/R0003",
          "name": "RANGOON ROAD",
          "address": "CITIGATE RESIDENCE, 168 RANGOON ROAD SINGAPORE 218437",
          "epsg4326": "+01.3165262+103.8531529/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 75,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 75,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 75,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "6259eef6f6c6c4fbe1c911e0baf3355b657d0eeed9091f9a62460f1ac48bb948"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 8418,
        "travelTime": 18,
        "lots": {},
        "asof": "2023-11-06T07:02:14.595019400Z"
      },
      {
        "id": "01HEHPDHP4SD3YN0X713YJPER5",
        "info": {
          "id": "01HEHPDHP4SD3YN0X713YJPER5",
          "ref": "ura/J0047",
          "name": "JALAN LEMBAH KALLANG",
          "address": "9 JALAN LEMBAH KALLANG SINGAPORE 339565",
          "epsg4326": "+01.3151479+103.8626648/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1020,
              "rate": 0,
              "minDuration": 0,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 8,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "e2b2de76ece41366abd2a1d6298aea6c0789f00fab862d88e45bfbbff66cf3c1"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10658,
        "travelTime": 19,
        "lots": {},
        "asof": "2023-11-06T07:02:15.176530600Z"
      },
      {
        "id": "01HEHPDHP5EXTNKSWD7NGA61CH",
        "info": {
          "id": "01HEHPDHP5EXTNKSWD7NGA61CH",
          "ref": "ura/K0120",
          "name": "KEMPAS ROAD",
          "address": "304 LAVENDER STREET SINGAPORE 338812",
          "epsg4326": "+01.3152065+103.8599587/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 0,
              "minDuration": 0,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 0,
              "minDuration": 0,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 26,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 0,
              "minDuration": 0,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 17,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "df181520eb2e1fcc7796927ba7cbdb50cdb2135f9c78a6194521b222380a2f61"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 9453,
        "travelTime": 16,
        "lots": {},
        "asof": "2023-11-06T07:02:15.818080Z"
      },
      {
        "id": "01HEHPDGKQHN8CBWNR4J3A2GZB",
        "info": {
          "id": "01HEHPDGKQHN8CBWNR4J3A2GZB",
          "ref": "hdb/BRM",
          "name": "BLK 39 BENDEMEER ROAD",
          "address": "THE RIVER VISTA @ KALLANG, 39 BENDEMEER ROAD SINGAPORE 330039",
          "epsg4326": "+01.3207472+103.8661729/",
          "lots": [],
          "features": [],
          "hash": "2b97c95f4612e3d125148bce92ef7340183d365953a9187eafac02d28bccad38"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 8623,
        "travelTime": 17,
        "lots": {
          "pip.vehicle_type/car": {
            "c": 312,
            "p": -1
          }
        },
        "asof": "2023-11-06T07:01:00Z"
      },
      {
        "id": "01HEHPDGKNX60M2C5YN787NGKR",
        "info": {
          "id": "01HEHPDGKNX60M2C5YN787NGKR",
          "ref": "ura/S0108",
          "name": "SHREWSBURY ROAD OFF STREET",
          "address": "KALLANG/WHAMPOA NEW TOWN,",
          "epsg4326": "+01.3186025+103.850678/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 29,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 5,
              "system": "pip.parking_system.electronic"
            }
          ],
          "features": [],
          "hash": "939d732153f1f2fd4aff69196d5faf624b4dec89a9a7ff2fdeaae3c116b5768f"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10180,
        "travelTime": 17,
        "lots": {
          "pip.vehicle_type/motorcycle": {
            "c": 3,
            "p": -1
          },
          "pip.vehicle_type/car": {
            "c": 26,
            "p": -1
          }
        },
        "asof": "2023-11-06T07:01:00Z"
      },
      {
        "id": "01HEHPDGKQW8QETR0HF9V0T9VX",
        "info": {
          "id": "01HEHPDGKQW8QETR0HF9V0T9VX",
          "ref": "hdb/BR9",
          "name": "BLK 69 MOULMEIN ROAD",
          "address": "MOULMEIN VIEW, 69 MOULMEIN ROAD SINGAPORE 300069",
          "epsg4326": "+01.3189626+103.8507431/",
          "lots": [],
          "features": [],
          "hash": "d8f994d9e43834ece5187a751f1e68bc8d5f252ce7ad25ac2579abc6fdb94712"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 9440,
        "travelTime": 15,
        "lots": {
          "pip.vehicle_type/car": {
            "c": 33,
            "p": -1
          }
        },
        "asof": "2023-11-06T07:01:00Z"
      },
      {
        "id": "01HEHPDHP7RSCKB1843Z4BAHTX",
        "info": {
          "id": "01HEHPDHP7RSCKB1843Z4BAHTX",
          "ref": "ura/S0045",
          "name": "SING AVENUE",
          "address": "50 JOO AVENUE SINGAPORE 219347",
          "epsg4326": "+01.3155884+103.8541906/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 19,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 19,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 19,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "ac74f933405a7f4b7f2e7fd3f9e4807268ad6d0e90d699700ac6ee6079a19e57"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 12036,
        "travelTime": 19,
        "lots": {},
        "asof": "2023-11-06T07:02:17.871449500Z"
      },
      {
        "id": "01HEHPDHP4SV0CTAP9SD8Z6FTZ",
        "info": {
          "id": "01HEHPDHP4SV0CTAP9SD8Z6FTZ",
          "ref": "ura/J0069",
          "name": "JOO AVENUE",
          "address": "22A JOO AVENUE SINGAPORE 219318",
          "epsg4326": "+01.3153989+103.8550523/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 28,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 28,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 28,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "ba0c1912654dc7964d7cadb942ae9eb2401de65367f2f9fc0cb56f786958f99c"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 12120,
        "travelTime": 20,
        "lots": {},
        "asof": "2023-11-06T07:02:18.066877400Z"
      },
      {
        "id": "01HEHPDGKNVSSJY0S8R4014F70",
        "info": {
          "id": "01HEHPDGKNVSSJY0S8R4014F70",
          "ref": "ura/S0049",
          "name": "Serangoon Rd-Lavender  St Off Street",
          "address": "UMAR PULAVAR TAMIL LANGUAGE CENTRE, 2 BEATTY ROAD SINGAPORE 209954",
          "epsg4326": "+01.3148875+103.8585876/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 500,
              "minDuration": 510,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 60,
              "minDuration": 30,
              "capacity": 112,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 420,
              "endTime": 1350,
              "rate": 65,
              "minDuration": 930,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 1350,
              "endTime": 420,
              "rate": 65,
              "minDuration": 510,
              "capacity": 25,
              "system": "pip.parking_system.electronic"
            }
          ],
          "features": [],
          "hash": "caca3efde299bb2f2c2279894e738ff379c814649c7099feed59f02a7bf8f9f5"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 10210,
        "travelTime": 19,
        "lots": {
          "pip.vehicle_type/motorcycle": {
            "c": 2,
            "p": -1
          },
          "pip.vehicle_type/car": {
            "c": 79,
            "p": -1
          }
        },
        "asof": "2023-11-06T07:01:00Z"
      },
      {
        "id": "01HEHPDHP86SP5V5TFR4ZF2RBG",
        "info": {
          "id": "01HEHPDHP86SP5V5TFR4ZF2RBG",
          "ref": "ura/T0057",
          "name": "TESSENSOHN ROAD",
          "address": "KALLANG/WHAMPOA NEW TOWN,",
          "epsg4326": "+01.3150063+103.8558668/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 40,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 40,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 60,
              "minDuration": 30,
              "capacity": 40,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/motorcycle",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1320,
              "rate": 65,
              "minDuration": 870,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "450ef39fe2c2c3e70dc579f0a43287a37920894602d0de801ed9485ac8229bad"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 12000,
        "travelTime": 19,
        "lots": {},
        "asof": "2023-11-06T07:02:18.487271500Z"
      },
      {
        "id": "01HEHPDHP8MYTVKP1AQ724MEG6",
        "info": {
          "id": "01HEHPDHP8MYTVKP1AQ724MEG6",
          "ref": "ura/S0119",
          "name": "SHREWSBURY ROAD",
          "address": "135 MOULMEIN ROAD SINGAPORE 308084",
          "epsg4326": "+01.3183082+103.848615/",
          "lots": [
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/saturday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/sunday_ph",
              "startTime": 510,
              "endTime": 1020,
              "rate": 0,
              "minDuration": 0,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            },
            {
              "vehicleType": "pip.vehicle_type/car",
              "chargeType": "pip.charge_type/weekday",
              "startTime": 510,
              "endTime": 1020,
              "rate": 60,
              "minDuration": 30,
              "capacity": 10,
              "system": "pip.parking_system/coupon"
            }
          ],
          "features": [],
          "hash": "61633ca1eaef3f56dc423853c2a90b46ff996117e6841f2c7a2c67dfe3750ef7"
        },
        "origin": "+01.3722655+103.8288232/",
        "distance": 9945,
        "travelTime": 16,
        "lots": {},
        "asof": "2023-11-06T07:02:19.082545800Z"
      }
    ]""".trimIndent()
    )
}