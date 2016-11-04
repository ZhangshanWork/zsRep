package im.vinci.server.naturelang.service;

import im.vinci.server.naturelang.domain.MusicSemantic;
import im.vinci.server.naturelang.domain.XMLYSemantic;
import im.vinci.server.search.domain.himalayas.GetHimalayaAlbumDetailResponse;

import java.util.List;

public interface NatureLangService {

	public List<MusicSemantic> getFinalResult(String str) throws Exception;

	public List<MusicSemantic> getNationFinalResult(String str) throws Exception;

	public List<XMLYSemantic> getXMLYFinalResult(String str) throws Exception;

	public List<String> getSingersResult(String str) throws Exception;

	public List<String> getFilteredSingersResult(String str) throws Exception;

    public GetHimalayaAlbumDetailResponse getAlbumById(String id);

}
