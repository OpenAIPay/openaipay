package cn.openaipay.infrastructure.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.domain.app.model.AppDevice;
import cn.openaipay.infrastructure.app.dataobject.AppDeviceDO;
import cn.openaipay.infrastructure.app.mapper.AppDeviceMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class AppDeviceRepositoryImplTest {

    @Mock
    private AppDeviceMapper appDeviceMapper;

    private AppDeviceRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AppDeviceRepositoryImpl(appDeviceMapper);
    }

    @Test
    void saveShouldRetryUpdateWhenInsertHitsDuplicateDeviceId() {
        String deviceId = "ios-device-vendor-duplicate";
        LocalDateTime now = LocalDateTime.of(2026, 3, 29, 10, 0, 0);
        AppDevice device = AppDevice.register(
                deviceId,
                "OPENAIPAY_IOS",
                List.of("ios-client-1"),
                "APPLE",
                "18.0",
                now
        );
        device.touchOpened(now.plusSeconds(1));
        device.bindLoginUser(
                880100068483692100L,
                "20880001",
                "13920000001",
                "ACTIVE",
                "L2",
                "顾郡",
                null,
                "13920000001",
                "顾*",
                "330***********",
                "CN",
                "F",
                "Hangzhou",
                now.plusSeconds(2)
        );

        AppDeviceDO existing = new AppDeviceDO();
        existing.setId(99L);
        existing.setDeviceId(deviceId);
        existing.setCreatedAt(now.minusDays(1));
        existing.setInstalledAt(now.minusDays(1));

        when(appDeviceMapper.findByDeviceId(deviceId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(appDeviceMapper.save(any(AppDeviceDO.class)))
                .thenThrow(new DuplicateKeyException("duplicate device id"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppDevice saved = repository.save(device);

        ArgumentCaptor<AppDeviceDO> savedCaptor = ArgumentCaptor.forClass(AppDeviceDO.class);
        verify(appDeviceMapper, times(2)).save(savedCaptor.capture());
        AppDeviceDO retriedEntity = savedCaptor.getAllValues().get(1);

        assertThat(retriedEntity.getId()).isEqualTo(existing.getId());
        assertThat(retriedEntity.getCreatedAt()).isEqualTo(existing.getCreatedAt());
        assertThat(retriedEntity.getInstalledAt()).isEqualTo(existing.getInstalledAt());
        assertThat(saved.getId()).isEqualTo(existing.getId());
    }
}
