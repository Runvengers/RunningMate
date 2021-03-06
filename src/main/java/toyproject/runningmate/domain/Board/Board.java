package toyproject.runningmate.domain.Board;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import toyproject.runningmate.domain.Address;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.BoardDto;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue
    @Column(name = "BOARD_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isClosed;

    private String meetingTime;

    @Enumerated(EnumType.STRING)
    private BoardCategory boardCategory;

    //바꾼 것
    @Embedded
    private Address address;

//    private String meetingPlace;
    //      selectbox1
    //  턱별/광역/도  시/구/군  읍/면/동  서울특별시 동작구 황도동
    //               ======

    private LocalDateTime regDate;
    private int count;
    private String image;
    private String openChat;

    @Builder
    public Board(User user, BoardCategory boardCategory, String title, String content, boolean isClosed, String meetingTime, Address address, LocalDateTime regDate, int count, String image, String openChat) {
        this.user = user;
        this.boardCategory = boardCategory;
        this.title = title;
        this.content = content;
        this.isClosed = isClosed;
        this.meetingTime = meetingTime;
        this.address = address;
        this.regDate = regDate;
        this.count = count;
        this.image = image;
        this.openChat = openChat;
    }

    public BoardDto toBoardDto(){
        return BoardDto.builder()
                .id(getId())
                .boardCategory(getBoardCategory())
                .userDto(getUser().toUserDto())
                .title(getTitle())
                .address(getAddress())
                .content(getContent())
                .isClosed(isClosed())
                .meetingTime(getMeetingTime())
                .regDate(getRegDate())
                .count(getCount())
                .image(getImage())
                .openChat(getOpenChat())
                .build();
    }

    public void update(String title, String content, Address address, String meetingTime, String image, String openChat) {
        this.title = title;
        this.content = content;
        this.address = address;
        this.meetingTime = meetingTime;
        this.image = image;
        this.openChat = openChat;
    }

    public Boolean changeIsClosed(Boolean closed) {
        isClosed = closed;

        return isClosed;
    }

    public void updateCount() {
        this.count++;
    }

    public void addUser(User user) {
        this.user = user;
    }


}
