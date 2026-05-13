package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.Room;
import com.example.smartcinemabookingsystem.model.Seat;
import com.example.smartcinemabookingsystem.repository.RoomRepository;
import com.example.smartcinemabookingsystem.repository.SeatRepository;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room saveRoom(Room room) {
        validateRoom(room);

        Room roomToSave;
        if (room.getId() == null) {
            roomToSave = new Room();
        } else {
            roomToSave = roomRepository.findById(room.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu."));
        }

        roomToSave.setName(room.getName().trim());
        roomToSave.setTotalSeats(room.getTotalSeats());

        Room savedRoom = roomRepository.save(roomToSave);
        ensureSeatCount(savedRoom);
        return savedRoom;
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy phòng chiếu.");
        }
        if (!showtimeRepository.findByRoomId(id).isEmpty()) {
            throw new IllegalStateException("Không thể xóa phòng đang có suất chiếu.");
        }
        roomRepository.deleteById(id);
    }

    private void validateRoom(Room room) {
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        }
        if (room.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Tổng số ghế phải lớn hơn 0.");
        }

        roomRepository.findByName(room.getName().trim()).ifPresent(existingRoom -> {
            if (room.getId() == null || !existingRoom.getId().equals(room.getId())) {
                throw new IllegalArgumentException("Tên phòng đã tồn tại.");
            }
        });
    }

    @Transactional
    public void ensureSeatCount(Room room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("Phòng chiếu không hợp lệ.");
        }

        Room managedRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu."));
        long currentSeatCount = seatRepository.countByRoomId(managedRoom.getId());
        int targetSeatCount = managedRoom.getTotalSeats();

        if (targetSeatCount < currentSeatCount) {
            throw new IllegalArgumentException("Không thể giảm số ghế thấp hơn số ghế đã tạo hiện tại: " + currentSeatCount);
        }

        for (long index = currentSeatCount + 1; index <= targetSeatCount; index++) {
            Seat seat = new Seat();
            seat.setRoom(managedRoom);
            seat.setSeatName(buildSeatName(index));
            seatRepository.save(seat);
        }
    }

    private String buildSeatName(long index) {
        long zeroBasedIndex = index - 1;
        char row = (char) ('A' + (zeroBasedIndex / 10));
        long number = (zeroBasedIndex % 10) + 1;
        return row + String.valueOf(number);
    }
}
