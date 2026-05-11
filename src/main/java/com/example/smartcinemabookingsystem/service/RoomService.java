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
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong chieu."));
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
            throw new IllegalArgumentException("Khong tim thay phong chieu.");
        }
        if (!showtimeRepository.findByRoomId(id).isEmpty()) {
            throw new IllegalStateException("Khong the xoa phong dang co suat chieu.");
        }
        roomRepository.deleteById(id);
    }

    private void validateRoom(Room room) {
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ten phong khong duoc de trong.");
        }
        if (room.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Tong so ghe phai lon hon 0.");
        }
    }

    @Transactional
    public void ensureSeatCount(Room room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("Phong chieu khong hop le.");
        }

        Room managedRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong chieu."));
        long currentSeatCount = seatRepository.countByRoomId(managedRoom.getId());
        int targetSeatCount = managedRoom.getTotalSeats();

        if (targetSeatCount < currentSeatCount) {
            throw new IllegalArgumentException("Khong the giam so ghe thap hon so ghe da tao hien tai: " + currentSeatCount);
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
