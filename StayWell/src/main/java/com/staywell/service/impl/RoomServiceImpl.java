package com.staywell.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.staywell.dto.RoomDTO;
import com.staywell.dto.UpdateDetailsDTO;
import com.staywell.enums.RoomType;
import com.staywell.exception.HotelException;
import com.staywell.exception.RoomException;
import com.staywell.model.Hotel;
import com.staywell.model.Reservation;
import com.staywell.model.Room;
import com.staywell.repository.HotelDao;
import com.staywell.repository.ReservationDao;
import com.staywell.repository.RoomDao;
import com.staywell.service.RoomService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

	private PasswordEncoder passwordEncoder;
	private HotelDao hotelDao;
	private RoomDao roomDao;
	private ReservationDao reservationDao;

	@Override
	public Room addRoom(RoomDTO roomDTO) {
		Hotel hotel = getCurrentLoggedInHotel();

		List<Room> rooms = hotel.getRooms();
		log.info("Validating Room number");
		for (Room r : rooms) {
			if (r.getRoomNumber() == roomDTO.getRoomNumber()) {
				throw new RoomException(
						"Room already present in your hotel with room number : " + roomDTO.getRoomNumber());
			}
		}

		Room room = buildRoom(roomDTO);

		log.info("Assigning room to the Hotel : " + hotel.getName());
		hotel.getRooms().add(room);
		room.setHotel(hotel);

		roomDao.save(room);

		log.info("Room saved successfully");
		return room;
	}

	@Override
	public String updateRoomType(UpdateDetailsDTO updateRequest, Long roomId) {
		Hotel hotel = getCurrentLoggedInHotel();

		log.info("Verifying credentials");
		if (!passwordEncoder.matches(updateRequest.getPassword(), hotel.getPassword())) {
			throw new RoomException("Wrong credentials!");
		}
		roomDao.setRoomType(roomId, RoomType.valueOf(updateRequest.getField()));

		log.info("Updation successfull");
		return "Updated Room Type to " + updateRequest.getField();
	}

	@Override
	public String updateNoOfPerson(UpdateDetailsDTO updateRequest, Long roomId) {
		Hotel hotel = getCurrentLoggedInHotel();

		log.info("Verifying credentials");
		if (!passwordEncoder.matches(updateRequest.getPassword(), hotel.getPassword())) {
			throw new RoomException("Wrong credentials!");
		}
		roomDao.setNoOfPerson(roomId, Integer.valueOf(updateRequest.getField()));

		log.info("Updation successfull");
		return "Updated number of person allowed per Room to " + updateRequest.getField();
	}

	@Override
	public String updatePrice(UpdateDetailsDTO updateRequest, Long roomId) {
		Hotel hotel = getCurrentLoggedInHotel();

		log.info("Verifying credentials");
		if (!passwordEncoder.matches(updateRequest.getPassword(), hotel.getPassword())) {
			throw new RoomException("Wrong credentials!");
		}
		roomDao.setPrice(roomId, BigDecimal.valueOf(Double.valueOf(updateRequest.getField())));

		log.info("Updation successfull");
		return "Updated Room Price to " + updateRequest.getField();
	}

	@Override
	public String updateAvailable(UpdateDetailsDTO updateRequest, Long roomId) {
		Hotel hotel = getCurrentLoggedInHotel();

		log.info("Verifying credentials");
		if (!passwordEncoder.matches(updateRequest.getPassword(), hotel.getPassword())) {
			throw new RoomException("Wrong credentials!");
		}
		roomDao.setAvailable(roomId, Boolean.valueOf(updateRequest.getField()));

		log.info("Updation successfull");
		return "Updated Room's availability to " + updateRequest.getField();
	}

	@Override
	public String removeRoom(UpdateDetailsDTO updateRequest) {
		Hotel hotel = getCurrentLoggedInHotel();

		log.info("Verifying credentials");
		if (!passwordEncoder.matches(updateRequest.getPassword(), hotel.getPassword())) {
			throw new RoomException("Wrong credentials!");
		}

		Long roomId = Long.valueOf(updateRequest.getField());
		Optional<Room> optional = roomDao.findById(roomId);
		if (optional.isEmpty())
			throw new RoomException("Room not found with Id : " + roomId);
		Room room = optional.get();

		List<Room> rooms = hotel.getRooms();
		List<Reservation> reservations = room.getReservations();

		log.info("Checking if this Room has any pending Reservations");
		for (Reservation r : reservations) {
			if (!r.getStatus().toString().equals("CLOSED")) {

				log.info("Room has pending Reservations");
				roomDao.setAvailable(roomId, false);

				throw new RoomException(
						"Booked Room can't be removed, but it is set to not available for future bookings");
			}
		}

		log.info("Removing reference of Room from every Reservation");
		for (Reservation r : reservations) {
			r.setRoom(null);
		}

		log.info("Removing reference of Room from Hotel");
		rooms.remove(room);
		hotel.setRooms(rooms);

		log.info("Deletion in progress");
		roomDao.delete(room);

		log.info("Room removed successfully");
		return "Room removed successfully";
	}

	@Override
	public List<Room> getAllAvailableRoomsByHotelId(Long hotelId, LocalDate checkIn, LocalDate checkOut) {

		Optional<Hotel> opt = hotelDao.findById(hotelId);
		if (opt.isEmpty())
			throw new HotelException("Hotel not found with id : " + hotelId);
		Hotel hotel = opt.get();

		List<Room> rooms = hotel.getRooms().stream().filter(h -> h.getAvailable()).collect(Collectors.toList());
		List<Reservation> reservations = reservationDao.getPendingReservationsOfHotel(hotel);
		for (Reservation r : reservations) {
			for (Room room : rooms) {
				if ((room.getRoomId() == r.getRoom().getRoomId())
						&& (checkIn.isEqual(r.getCheckinDate()) || checkIn.isEqual(r.getCheckinDate()))
						|| (checkOut.isEqual(r.getCheckinDate()) || checkOut.isEqual(r.getCheckinDate()))
						|| (checkIn.isAfter(r.getCheckinDate()) && checkIn.isBefore(r.getCheckinDate()))
						|| (checkOut.isAfter(r.getCheckinDate()) && checkOut.isBefore(r.getCheckinDate()))) {
					rooms.remove(room);
				}
			}
		}

		if (rooms.isEmpty())
			throw new RoomException("Rooms not found in this hotel");
		return rooms;
	}

	@Override
	public List<Room> getAllRoomsByHotel() {
		Hotel hotel = getCurrentLoggedInHotel();
		List<Room> rooms = hotel.getRooms();
		if (rooms.isEmpty())
			throw new RoomException("No rooms found");
		return rooms;
	}

	private Hotel getCurrentLoggedInHotel() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return hotelDao.findByHotelEmail(email)
				.orElseThrow(() -> new HotelException("Failed to fetch the hotel with the email: " + email));
	}

	private Room buildRoom(RoomDTO roomDTO) {
		return Room.builder()
				.roomNumber(roomDTO.getRoomNumber())
				.roomType(roomDTO.getRoomType())
				.noOfPerson(roomDTO.getNoOfPerson())
				.price(roomDTO.getPrice())
				.available(roomDTO.getAvailable())
				.reservations(new ArrayList<>())
				.build();
	}

}