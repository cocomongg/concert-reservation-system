# ER Diagram

![erd1](https://github.com/user-attachments/assets/411ddcad-a803-420c-984d-732d9a3d1c44)
<br/>

# Description
- `waiting_queue` 테이블은 `user`테이블의 pk를 가지고 있지만, 연관관계는 없는 것 같아, 맺지 않았다.
- `concert_seat` 테이블의 `temp_reserved_at` 컬럼을 통해 좌석 임시 배정 상태에 대한 관리를 한다.
  - 해당 좌석의 임시 배정된 시간을 기록하여 사용자가 임시배정된 좌석에 대한 점유 시도 시, `temp_reserved_at` 컬럼의 시간과,<br/>
    점유 요청 시간을 비교하여 점유를 할 수 있는지, 아직 임시배정이 유지중인지를 판단할 수 있다.
- `payment`와 `paymet_history`를 1대다 연관관계를 맺어, 해당 결제 건에 대한 [결제], [환불] 등 상태 변경에 대한 기록을 할 수 있다.